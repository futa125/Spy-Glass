package com.piratesofcode.spyglass.utils

import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText

object Extensions {

    fun TextInputEditText.validateEmail() =
        this.text.toString().matches(Patterns.EMAIL_ADDRESS.toRegex())

    fun TextInputEditText.validatePassword() =
        this.text.toString().length >= Constants.PASSWORD_MIN_LENGTH

    fun TextInputEditText.onTextChanged(validate: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate.invoke(s.toString())
            }
        })
    }

}