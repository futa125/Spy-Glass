package com.piratesofcode.spyglass.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.piratesofcode.spyglass.R
import com.piratesofcode.spyglass.api.AuthenticationRepository
import com.piratesofcode.spyglass.api.DocumentRepository
import com.piratesofcode.spyglass.databinding.FragmentHistoryBinding
import com.piratesofcode.spyglass.model.document.DocumentStatus
import com.piratesofcode.spyglass.model.user.UserRole
import com.piratesofcode.spyglass.ui.main.adapters.DocumentsAdapter
import com.piratesofcode.spyglass.viewmodels.DocumentsViewModel


class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!
    private val viewModel: DocumentsViewModel by viewModels()

    private val adapter by lazy { DocumentsAdapter(requireContext()) }

    private var switchChecked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.adapter = adapter
        binding.str.isRefreshing = true
        binding.str.setOnRefreshListener {
            getDocuments(!switchChecked)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (AuthenticationRepository.user!!.role == UserRole.EMPLOYEE) {
            binding.historySwitch.visibility = View.GONE
        }

        setupListeners()
        setupObservers()

        if (savedInstanceState == null) {
            getDocuments(personal = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (removeWithId.isNotBlank()) {
            adapter.removeItem(removeWithId)
            removeWithId = ""
        }

        if (adapter.itemCount == 0) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
    }

    private fun getDocuments(personal: Boolean) {
        binding.apply {
            str.isRefreshing = true

            if (personal) {
                adapter.showRoleControls = false
                viewModel.getDocumentsByUser(AuthenticationRepository.user!!)
            } else {
                adapter.showRoleControls = true
                when (AuthenticationRepository.user!!.role) {
                    UserRole.EMPLOYEE ->
                        throw IllegalStateException("Employee shouldn't be able to toggle this.")
                    UserRole.REVISER ->
                        viewModel.getDocumentsByStatus(
                            DocumentStatus(
                                archived = false,
                                signed = false,
                                toBeSigned = false,
                                revised = false,
                                scannedProperly = true
                            )
                        )
                    UserRole.ACCOUNTANT ->
                        viewModel.getDocumentsByStatus(
                            DocumentStatus(
                                archived = false,
                                signed = false,
                                toBeSigned = false,
                                revised = true,
                                scannedProperly = true
                            ),
                            DocumentStatus(
                                archived = false,
                                signed = true,
                                toBeSigned = true,
                                revised = true,
                                scannedProperly = true
                            )
                        )
                    UserRole.DIRECTOR ->
                        viewModel.getDocumentsByStatus(
                            DocumentStatus(
                                archived = false,
                                signed = false,
                                toBeSigned = true,
                                revised = true,
                                scannedProperly = true
                            )
                        )
                }
            }

        }
    }

    private fun setupListeners() {
        binding.apply {
            historySwitch.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                switchChecked = checked
                if (checked) {
                    DocumentRepository.historySwitchPersonal = false
                    when (AuthenticationRepository.user!!.role) {
                        UserRole.REVISER -> binding.historySwitch.text =
                            resources.getString(R.string.history_switch_reviser)
                        UserRole.ACCOUNTANT -> binding.historySwitch.text =
                            resources.getString(R.string.history_switch_accountant)
                        UserRole.DIRECTOR -> binding.historySwitch.text =
                            resources.getString(R.string.history_switch_director)
                        UserRole.EMPLOYEE ->
                            throw IllegalStateException("Employee shouldn't be able to toggle this")
                    }
                    getDocuments(personal = false)
                } else {
                    DocumentRepository.historySwitchPersonal = true
                    binding.historySwitch.text =
                        resources.getString(R.string.history_switch_personal)
                    getDocuments(personal = true)
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.getDocumentsLiveData().observe(
            viewLifecycleOwner
        ) { documents ->
            binding.str.isRefreshing = false
            if (documents.isNotEmpty()) {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.setDocuments(documents)
            } else {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }
        }
    }

    companion object {
        var removeWithId = ""
    }
}