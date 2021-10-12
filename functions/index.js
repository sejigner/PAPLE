const functions = require("firebase-functions");

const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyNewMessage = functions.database
    .ref('/User-messages/{UID}')
    .onCreate((snapshot, context) => {
        const message = snapshot.data();
        const recipientId = message['toId'];
        

        return functions.database().ref('Users/' + recipientId).get().then(userDoc => {
            const registrationToken = userDoc.get('registrationToken')
            const senderNickname = userDoc.get('nickname')
            const notificationBody = message['message']
            const payload = {
                notification: {
                    title: senderNickname + "님이 메세지를 보냈어요",
                    body : notificationBody,
                    clickAction : "ChatLogActivity"
                },
                data: {
                    MYNICKNAME : senderNickname,
                    UID: message['fromId']
                }
            }

            return admin.messaging().sendToDevice(registrationToken, payload).then(response => {
                const stillRegisteredTokens = registrationToken

                response.results.forEach((result, index) => {
                    const error = result.error
                    if(error) {
                        const failedRegistrationToken = registrationToken[index]
                        console.error('push noti error', failedRegistrationToken, error)
                        if(error.code === 'messaging/invalid=registration-token'
                        || error.code === 'messaging/registration-token-not-registered')
                        const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken)
                        if(failedIndex > -1) {
                            stillRegisteredTokens.splice(failedIndex, 1)
                        }
                    }
                })
                return functions.database.ref("Users/" + toId).update({
                    registrationTokens: stillRegisteredTokens
                })
            })
        })
    })