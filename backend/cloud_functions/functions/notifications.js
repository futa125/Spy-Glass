const functions = require("firebase-functions");
const admin = require("firebase-admin");

exports.directorNotif =
  functions.firestore.document("/documentStatus/{doc}")
    .onUpdate(async (change, context) => {
      const db = admin.firestore();

      const after = change.after.data();
      const before = change.before.data();
      const docID = context.params.doc;

      if (after.toBeSigned === true && before.toBeSigned === false) {
        const userCollection = db.collection("users");
        const dbFCM = db.collection("FCMTokens");

        const directorsSnapshot = await userCollection.where("role", "==", "director").get();

        directorsSnapshot.forEach(async (director) => {
          let userToken = (await dbFCM.doc(director.id).get()).data();
          if (userToken !== undefined) {
            const message = {
              token: userToken.FCMToken,
              data: {
                documentId: docID,
                notificationTitle: "New document to sign!",
                notificationBody: `Tap this notification to open the document.`
              }
            };
            console.log(`sending notification to director with ID: ${director.id} using FCM Token: ${userToken.FCMToken}`);
            await admin.messaging().send(message);
          }
        });
      }
    }
    );

exports.accountantNotif =
  functions.firestore.document("documentStatus/{doc}")
    .onUpdate(async (change, context) => {
      const db = admin.firestore();

      const after = change.after.data();
      const before = change.before.data();
      const docID = context.params.doc;

      if (after.signed == true && before.signed == false) {
        const dbFCM = db.collection("FCMTokens");
        const accountantId = (await db.collection("documentByUser").doc(docID).get()).data().sentToSignBy;
        const accountant = (await db.collection("users").doc(accountantId).get())
        console.log(`accountant exists: ${accountant.exists}`)
        if(accountant.exists) {
          let userToken = (await dbFCM.doc(accountant.id).get()).data();
          if (userToken !== undefined) {
            const message = {
              token: userToken.FCMToken,
              data: {
                documentId: docID,
                notificationTitle: "New document to archive!",
                notificationBody: `Tap this notification to open the document.`
              }
            };
            console.log(`sending notification to user with ID: ${accountant.id} using FCM Token: ${userToken.FCMToken}`);
            await admin.messaging()
              .send(message);
          }
        }else{
          const document = await db.collection("documents").doc(docID).get();
          const docType = document.data().type;
          const userQuerySnapshot = await db.collection("users")
            .where("role", "==", "accountant")
            .where("specialization", "==", docType).get();
            userQuerySnapshot.forEach(async (accountant) => {
              let userToken = (await dbFCM.doc(accountant.id).get()).data();
              if (userToken !== undefined) {
                const message = {
                  token: userToken.FCMToken,
                  data: {
                    documentId: docID,
                    notificationTitle: "New document to archive!",
                    notificationBody: `Tap this notification to open the document.`
                  }
                };
                console.log(`sending notification to user with ID: ${accountant.id} using FCM Token: ${userToken.FCMToken}`);
                await admin.messaging()
                  .send(message);
              }
            })
        }
      }
    });
