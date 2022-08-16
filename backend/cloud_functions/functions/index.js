const counter = require('./counter');
const notifications = require("./notifications");

exports.directorNotif = notifications.directorNotif;
exports.accountantNotif = notifications.accountantNotif;

exports.onUserCreate = counter.onUserCreate;
exports.onUserDelete = counter.onUserDelete;
exports.onStatusCreate = counter.onStatusCreate;
exports.onStatusUpdate = counter.onStatusUpdate;
