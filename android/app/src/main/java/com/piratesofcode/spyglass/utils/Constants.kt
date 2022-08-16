package com.piratesofcode.spyglass.utils

import android.Manifest

object Constants {
    const val EXTRA_ACTIVATED_BY_NOTIFICATION = "ACTIVATED_BY_NOTIFICATION"
    const val EXTRA_CONTROLS = "EXTRA_CONTROLS"
    const val EXTRA_DOCUMENT = "EXTRA_DOCUMENT"
    const val EXTRA_TEXT = "EXTRA_TEXT"
    const val NOTIFICATION_CHANNEL_ID = "0"
    const val PASSWORD_MIN_LENGTH = 6
    const val TAG = "SPYGLASS_DEBUG"

    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
}