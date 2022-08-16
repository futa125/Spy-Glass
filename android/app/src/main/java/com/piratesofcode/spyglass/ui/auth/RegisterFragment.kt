package com.piratesofcode.spyglass.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.piratesofcode.spyglass.R
import com.piratesofcode.spyglass.databinding.DialogDocumentTypeBinding
import com.piratesofcode.spyglass.databinding.FragmentRegisterBinding
import com.piratesofcode.spyglass.model.user.UserRole
import com.piratesofcode.spyglass.ui.main.MainActivity
import com.piratesofcode.spyglass.utils.Extensions.onTextChanged
import com.piratesofcode.spyglass.utils.Extensions.validateEmail
import com.piratesofcode.spyglass.utils.Extensions.validatePassword
import com.piratesofcode.spyglass.viewmodels.AuthenticationViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AuthenticationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRoleMenu()
        setupListeners()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            emailText.onTextChanged { validateInput() }
            passwordText.onTextChanged { validateInput() }
            passwordConfirmText.onTextChanged { validateInput() }
            firstNameText.onTextChanged { validateInput() }
            lastNameText.onTextChanged { validateInput() }
        }
    }

    private fun setupObservers() {
        viewModel.getRegistrationResultLiveData().observe(
            viewLifecycleOwner,
            { authResult ->
                if (authResult) {
                    viewModel.storeFCMTokenToDB()
                    MainActivity.start(requireContext(), requireActivity())
                } else {
                    binding.registerProgress.visibility = View.GONE
                    Toast.makeText(context, "Email is taken.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupRoleMenu() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item,
            UserRole.values().sortedBy { it.ordinal }.map { it.value })
        binding.roleMenuTv.setText(UserRole.EMPLOYEE.value, false)
        (binding.roleMenuTv as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.apply {
            registerButton.setOnClickListener {
                if (roleMenuTv.text.toString() == UserRole.ACCOUNTANT.value) {
                    val dialogBinding = DialogDocumentTypeBinding.inflate(layoutInflater)
                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(dialogBinding.root)
                        .create()

                    dialog.window?.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rectangle_corner_radius_8dp
                        )
                    )

                    dialogBinding.registerButton.setOnClickListener {
                        val documentTypeString = dialogBinding.radioGroup.children
                            .filter { (it as RadioButton).isChecked }
                            .map { it as RadioButton }
                            .take(1)
                            .toList().first().text.toString()

                        dialog.dismiss()
                        register(
                            emailText.text.toString(),
                            passwordText.text.toString(),
                            firstNameText.text.toString(),
                            lastNameText.text.toString(),
                            roleMenuTv.text.toString(),
                            documentTypeString
                        )
                    }
                    dialog.show()
                } else {
                    register(
                        emailText.text.toString(),
                        passwordText.text.toString(),
                        firstNameText.text.toString(),
                        lastNameText.text.toString(),
                        roleMenuTv.text.toString()
                    )
                }
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

            passwordConfirmText.onFocusChangeListener = View.OnFocusChangeListener { _, focus ->
                if (passwordConfirmText.text.toString().isNotBlank() &&
                    !focus && binding.passwordText.text.toString() != binding.passwordConfirmText.text.toString()
                ) {
                    passwordConfirmField.error = getString(R.string.password_no_match)
                } else {
                    passwordConfirmField.error = null
                }
            }
        }
    }

    private fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String,
        specialization: String? = null
    ) {
        binding.registerProgress.visibility = View.VISIBLE
        viewModel.register(
            email,
            password,
            firstName,
            lastName,
            role,
            specialization
        )
    }

    private fun validateInput() {
        binding.apply {
            registerButton.isEnabled =
                passwordText.validatePassword() &&
                        emailText.validateEmail() &&
                        firstNameText.text.isNullOrBlank().not() &&
                        lastNameText.text.isNullOrBlank().not() &&
                        passwordText.text.toString() == passwordConfirmText.text.toString()
        }
    }
}
