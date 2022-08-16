package com.piratesofcode.spyglass.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.piratesofcode.spyglass.databinding.ActivityLoginRegisterBinding

class AuthenticationActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginRegisterBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
    }

    companion object {

        @JvmStatic
        fun start(context: Context, activity: Activity) =
            Intent(context, AuthenticationActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) }
                .also {
                    context.startActivity(it)
                    activity.finish()
                }
    }
}
