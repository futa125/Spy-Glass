package com.piratesofcode.spyglass.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.piratesofcode.spyglass.R
import com.piratesofcode.spyglass.api.AuthenticationRepository
import com.piratesofcode.spyglass.databinding.FragmentInfoBinding
import com.piratesofcode.spyglass.model.user.Accountant
import com.piratesofcode.spyglass.model.user.UserRole
import com.piratesofcode.spyglass.ui.auth.AuthenticationActivity
import com.piratesofcode.spyglass.ui.statistics.StatisticsActivity
import com.piratesofcode.spyglass.viewmodels.AuthenticationViewModel


class InfoFragment : Fragment() {

    private lateinit var binding: FragmentInfoBinding
    private val user = AuthenticationRepository.user
    private val viewModel by activityViewModels<AuthenticationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.statsButton.setOnClickListener {
            StatisticsActivity.start(requireContext())
        }

        showUserData()
        setupLogoutListener()
    }

    private fun showUserData() {
        binding.apply {
            if (user != null) {
                firstName.text = resources.getString(R.string.first_name, user.firstName)
                lastName.text = resources.getString(R.string.last_name, user.lastName)
                userRole.text = resources.getString(R.string.role, user.role.value.lowercase())
                userEmail.text = resources.getString(R.string.e_mail, user.email)

                if (user.role == UserRole.ACCOUNTANT) {
                    user as Accountant
                    userSpecialization.text = resources.getString(
                        R.string.specialization,
                        user.specialization.value.lowercase()
                    )
                } else {
                    userSpecialization.visibility = View.GONE
                }

                if (user.role != UserRole.DIRECTOR) {
                    statsButton.visibility = View.GONE
                }

            }
        }
    }

    private fun setupLogoutListener() {
        binding.apply {
            logoutButton.setOnClickListener {
                viewModel.logout()
                AuthenticationActivity.start(requireContext(), requireActivity())
            }
        }
    }
}