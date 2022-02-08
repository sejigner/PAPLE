'use strict';

const functions = require('firebase-functions');

const admin = require('firebase-admin');
const { ref } = require('firebase-functions/v1/database');
admin.initializeApp();



exports.deleteUser = functions.database.ref('/Users/{uid}')
    .onDelete(async (change, context) => {
        const uid = context.params.uid;

        functions.logger.log(
            uid, 'has deleted its user info'
        );

        let id = uid.toString();

        // Delete User from Firebase Auth
        admin.auth().deleteUser(id)
        .then(function() {
            console.log('Successfully deleted user');
          })
          .catch(function(error) {
            console.log('Error deleting user:', error);
          });

        var userLocationRef = admin.database().ref(`/User-Location/${id}`)
        userLocationRef.remove()
        .then(function(){
            console.log("Location info remove succeeded.")
        })
        .catch(function(error){
            console.log("Location info remove failed: " + error.message)
        })
    });

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
        if (!tokensSnapshot.hasChildren()) {
            return functions.logger.log(
                'There are no registration tokens to send to.'
            );
        }
        const senderNickname = senderNicknameSnapshot.val();
        console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
        console.log('Fetched sender profile', senderNickname);

        const payload = {
            data: {
                title: senderNickname,
                body: '메시지가 도착했어요!',
                sender : senderUid,
                type: 'message'
            }
        };

        const options = {
            priority: "high"
        };

        // Listing all tokens as an array.
        tokens = Object.keys(tokensSnapshot.val());

        // Send notifications to all tokens.
        const response = await admin.messaging().sendToDevice(tokens, payload, options);

        // For each message check if there was an error.
        const tokensToRemove = [];
        response.results.forEach((result, index) => {
            const error = result.error;
            if (error) {
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
exports.notifyNewPlane = functions.database.ref('/PaperPlanes/Receiver/{recipientUid}/{senderUid}')
    .onWrite(async (change, context) => {
        const recipientUid = context.params.recipientUid;
        const senderUid = context.params.senderUid;

        functions.logger.log(
            'New Plane from:',
            senderUid,
            'for user:',
            recipientUid
        );

        const getDeviceTokensPromise = admin.database()
            .ref(`/Users/${recipientUid}/registrationToken`).once('value');

        const getFlightDistance = admin.database()
            .ref(`/PaperPlanes/Receiver/${recipientUid}/${senderUid}/flightDistance`).once('value');

        const getPlaneMessage = admin.database()
            .ref(`/PaperPlanes/Receiver/${recipientUid}/${senderUid}/text`).once('value');

        let tokensSnapshot;
        let flightDistanceSnapshot;
        let planeMessageSnapshot;
        let tokens;

        const results = await Promise.all([getDeviceTokensPromise, getFlightDistance, getPlaneMessage]);
        tokensSnapshot = results[0];
        flightDistanceSnapshot = results[1];
        planeMessageSnapshot = results[2];

        // check if there are any device tokens.
        if (!tokensSnapshot.hasChildren()) {
            return functions.logger.log(
                'There are no registration tokens to send to.'
            );
        }
        const flightDistance = flightDistanceSnapshot.val();
        const planeMessage = planeMessageSnapshot.val();

        console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
        console.log('got flight distance', flightDistance);
        console.log('got plane message', planeMessage);

        const payload = {
            data: {
                title: (flightDistance + 'm 거리에서 비행기가 날아왔어요!'),
                body: planeMessage,
                sender : senderUid,
                type: 'plane'
            }
        };

        const options = {
            priority: "high"
        };

        // Listing all tokens as an array.
        tokens = Object.keys(tokensSnapshot.val());

        // Send notifications to all tokens.
        const response = await admin.messaging().sendToDevice(tokens, payload, options);

        // For each message check if there was an error.
        const tokensToRemove = [];
        response.results.forEach((result, index) => {
            const error = result.error;
            if (error) {
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