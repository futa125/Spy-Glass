const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

exports.onUserCreate = functions
.firestore
.document('users/{userId}')
.onCreate(async (snap, context) => {
    const userId = context.params.userId;

    const userStats = db.collection('userStats');
    await userStats.doc(userId).set({
        archivedNo: 0,
        revisedNo: 0,
        scannedProperlyNo: 0,
        scannedImproperlyNo: 0,
        signedNo: 0,
    });
});

exports.onUserDelete = functions
.firestore
.document('users/{userId}')
.onDelete(async (snap, context) => {
    const userId = context.params.userId;

    const userStats = db.collection('userStats');
    await userStats.doc(userId).delete();
})

exports.onStatusCreate = functions
.firestore
.document('documentStatus/{statusId}')
.onCreate(async (snap, context) => {
    const statusId = context.params.statusId;
    const status = snap.data();

    const document = await db.collection('documents').doc(statusId).get()
    const userId = await document.data().user;

    const userStats = db.collection('userStats');
    const oldUserStats = (await userStats.doc(userId).get()).data()

    if (status.scannedProperly === true) {
        await userStats.doc(userId).update('scannedProperlyNo', oldUserStats.scannedProperlyNo + 1);
    } else {
        await userStats.doc(userId).update('scannedImproperlyNo', oldUserStats.scannedImproperlyNo + 1);
    }

    if (status.revised === true) {
        await userStats.doc(userId).update('revisedNo', oldUserStats.revisedNo + 1);
    }

})

exports.onStatusUpdate = functions
.firestore
.document('documentStatus/{statusId}')
.onUpdate(async (change, context) => {
    const statusId = context.params.statusId;

    const newValues = change.after.data();
    const oldValues = change.before.data();

    var document = await db.collection('documents').doc(statusId).get()

    const userStats = db.collection('userStats');
    const docByUser = db.collection('documentByUser');

    if (newValues.archived === true && oldValues.archived === false) {
        let archivedById = (await docByUser.doc(document.id).get()).data().archivedBy;
        const oldArchiverStats = (await userStats.doc(archivedById).get()).data();
        await userStats.doc(archivedById).update('archivedNo', oldArchiverStats.archivedNo + 1);
    }

    if (newValues.signed === true && oldValues.signed === false) {
        let signedById = (await docByUser.doc(document.id).get()).data().signedBy; 
        const oldDirectorStats = (await userStats.doc(signedById).get()).data();
        await userStats.doc(signedById).update('signedNo', oldDirectorStats.signedNo + 1);
    }
})