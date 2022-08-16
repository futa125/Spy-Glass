package com.piratesofcode.spyglass.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.piratesofcode.spyglass.R
import com.piratesofcode.spyglass.databinding.FragmentLoginBinding
import com.piratesofcode.spyglass.model.document.Document
import com.piratesofcode.spyglass.ui.document.DocumentActivity
import com.piratesofcode.spyglass.ui.main.MainActivity
import com.piratesofcode.spyglass.utils.Constants.EXTRA_ACTIVATED_BY_NOTIFICATION
import com.piratesofcode.spyglass.utils.Constants.EXTRA_DOCUMENT
import com.piratesofcode.spyglass.utils.Constants.TAG
import com.piratesofcode.spyglass.utils.Extensions.onTextChanged
import com.piratesofcode.spyglass.utils.Extensions.validateEmail
import com.piratesofcode.spyglass.utils.Extensions.validatePassword
import com.piratesofcode.spyglass.utils.NotificationHelper
import com.piratesofcode.spyglass.viewmodels.AuthenticationViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AuthenticationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkUserLoggedIn()
        setupListeners()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        binding.emailText.onTextChanged { validateInput() }
        binding.passwordText.onTextChanged { validateInput() }
    }

    private fun setupObservers() {
        viewModel.getLoginResultLiveData().observe(
            viewLifecycleOwner,
            { authResult ->
                when (authResult) {
                    true -> {
                        viewModel.storeFCMTokenToDB()

                        val activatedByNotification =
                            activity?.intent?.extras?.getBoolean(EXTRA_ACTIVATED_BY_NOTIFICATION)
                        if (activatedByNotification != null && activatedByNotification == true) {
                            val document =
                                activity?.intent?.getSerializableExtra(EXTRA_DOCUMENT) as Document?
                            if (document != null) {
                                val intent = NotificationHelper.createNotificationClickIntent(
                                    activity!!.applicationContext,
                                    document,
                                    DocumentActivity::class.java
                                )
                                startActivity(intent)
                                activity?.finish()
                            } else {
                                Log.d(
                                    TAG,
                                    "intent.extras $EXTRA_DOCUMENT payload has been lost in the chain"
                                )
                                MainActivity.start(requireContext(), requireActivity())
                            }
                        } else {
                            MainActivity.start(requireContext(), requireActivity())
                        }
                    }
                    false -> {
                        binding.loginProgress.visibility = View.GONE
                        Toast.makeText(context, "Wrong credentials.", Toast.LENGTH_SHORT).show()
                    }
                    else -> binding.loginProgress.visibility = View.GONE
                }
            })
    }

    private fun setupListeners() {
        binding.apply {
            loginButton.setOnClickListener {
                loginProgress.visibility = View.VISIBLE
                viewModel.login(
                    binding.emailText.text.toString(),
                    binding.passwordText.text.toString()
                )
            }

            registerButton.setOnClickListener {
                findNavController().navigate(R.id.login_to_register)
            }

            emailText.onFocusChangeListener = View.OnFocusChangeListener { _, focus ->
                if (emailText.text.toString().isNotBlank() && !focus && !emailText.validateEmail()
                ) {
                    emailField.error = getString(R.string.email_invalid)
                } else {
                    emailField.error = null
                }
            }

            passwordText.onFocusChangeListener = View.OnFocusChangeListener { _, focus ->
                if (passwordText.text.toString()
                        .isNotBlank() && !focus && !passwordText.validatePassword()
                ) {
                    passwordField.error = getString(R.string.password_invalid)
                } else {
                    passwordField.error = null
                }
            }
        }
    }

    private fun validateInput() {
        binding.apply {
            loginButton.isEnabled = passwordText.validatePassword() && emailText.validateEmail()
        }
    }

    private fun checkUserLoggedIn() {
        binding.apply { loginProgress.visibility = View.VISIBLE }
        viewModel.checkUserLoggedIn()
    }
}
