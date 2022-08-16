package com.piratesofcode.spyglass.api

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.piratesofcode.spyglass.model.stats.UserStats
import com.piratesofcode.spyglass.utils.StatsHelper
import kotlinx.coroutines.tasks.await

object StatisticsRepository {
    private val userStatistics = Firebase.firestore.collection("userStats")

    suspend fun getUserStats(): List<UserStats> {
        val stats: MutableList<UserStats> = mutableListOf()
        val statsQuerySnapshot = userStatistics.get().await()

        for (statsSnapshot in statsQuerySnapshot) {
            val user = AuthenticationRepository.getUserFromFirestore(statsSnapshot.id)
            val stat = StatsHelper.createStatsFromSnapshot(statsSnapshot, user)
            stats.add(stat)
        }

        return stats
    }
}