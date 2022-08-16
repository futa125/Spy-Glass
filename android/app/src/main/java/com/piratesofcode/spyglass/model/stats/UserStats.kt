package com.piratesofcode.spyglass.model.stats

import com.piratesofcode.spyglass.model.user.UserRole

data class UserStats(
    val firstName: String,
    val lastName: String,
    val userRole: UserRole,
    val archivedNo: Int = 0,
    val revisedNo: Int = 0,
    val scannedProperlyNo: Int = 0,
    val scannedImproperlyNo: Int = 0,
    val signedNo: Int = 0
)