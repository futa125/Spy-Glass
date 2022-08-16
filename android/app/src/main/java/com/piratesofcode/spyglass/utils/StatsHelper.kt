package com.piratesofcode.spyglass.utils

import com.google.firebase.firestore.DocumentSnapshot
import com.piratesofcode.spyglass.model.stats.UserStats
import com.piratesofcode.spyglass.model.user.User

object StatsHelper {

    fun createStatsFromSnapshot(
        statsSnapshot: DocumentSnapshot,
        user: User
    ) = UserStats(
        user.firstName,
        user.lastName,
        user.role,
        statsSnapshot["archivedNo"].toString().toInt(),
        statsSnapshot["revisedNo"].toString().toInt(),
        statsSnapshot["scannedProperlyNo"].toString().toInt(),
        statsSnapshot["scannedImproperlyNo"].toString().toInt(),
        statsSnapshot["signedNo"].toString().toInt()
    )
}