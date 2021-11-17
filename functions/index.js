'use strict';

const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyNewMessage = functions.database.ref('/User-messages/{recipientUid}/{senderUid}/{messageId}')
    .onWrite(async (change, context) => {
        const recipientUid = context.params.recipientUid;
        const senderUid = context.params.senderUid;
        
        functions.logger.log(
            'New Message from:',
            senderUid,
            'for user:',
            recipientUid
          );

          const getDeviceTokensPromise = admin.database()
          .ref(`/Users/${recipientUid}/registrationToken`).once('value');

          const getSenderProfilePromise = admin.database()
          .ref(`/Users/${senderUid}/nickname`).once('value');

          let tokensSnapshot;
          let senderNicknameSnapshot;

          let tokens;

          const results = await Promise.all([getDeviceTokensPromise, getSenderProfilePromise]);
          tokensSnapshot = results[0];
          senderNicknameSnapshot = results[1];

    // check if there are any device tokens.
          if(!tokensSnapshot.hasChildren()) {
              return functions.logger.log(
                  'There are no registration tokens to send to.'
              );
          }
            const senderNickname = senderNicknameSnapshot.val();
            console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
            console.log('Fetched sender profile', senderNickname);

          const payload = {
              notification: {
                  title: senderNickname,
                  body: '메시지가 도착했어요!'
              }
          };
          
          // Listing all tokens as an array.
          tokens = Object.keys(tokensSnapshot.val());

          // Send notifications to all tokens.
          const response = await admin.messaging().sendToDevice(tokens, payload);
          
          // For each message check if there was an error.
          const tokensToRemove = [];
          response.results.forEach((result, index) => {
              const error = result.error;
              if(error) {
                  functions.logger.error(
                      'Failure sending notification to',
                      tokens[index],
                      error
                  );
                  // Cleanup the tokens who are not registered anymore.
                  if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                }
              }
          });
          return Promise.all(tokensToRemove);
        });