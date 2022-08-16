package com.piratesofcode.spyglass.ui.document

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import com.piratesofcode.spyglass.R
import com.piratesofcode.spyglass.api.AuthenticationRepository
import com.piratesofcode.spyglass.databinding.ActivityDocumentBinding
import com.piratesofcode.spyglass.model.document.*
import com.piratesofcode.spyglass.model.user.UserRole
import com.piratesofcode.spyglass.ui.document.adapter.ItemsAdapter
import com.piratesofcode.spyglass.ui.main.HistoryFragment
import com.piratesofcode.spyglass.utils.Constants.EXTRA_ACTIVATED_BY_NOTIFICATION
import com.piratesofcode.spyglass.utils.Constants.EXTRA_CONTROLS
import com.piratesofcode.spyglass.utils.Constants.EXTRA_DOCUMENT
import com.piratesofcode.spyglass.utils.Constants.EXTRA_TEXT
import kotlin.math.min


class DocumentActivity : AppCompatActivity() {

    private val binding by lazy { ActivityDocumentBinding.inflate(layoutInflater) }
    private val viewModel: DocumentViewModel by viewModels()

    private lateinit var document: Document

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.getStatusLiveData().observe(this) { message ->
            Toast.makeText(this@DocumentActivity, message, Toast.LENGTH_SHORT).show()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val activatedByNotification = intent.extras?.getBoolean(EXTRA_ACTIVATED_BY_NOTIFICATION)
        if (activatedByNotification != null && activatedByNotification == true) {
            val tempDocument = intent.getSerializableExtra(EXTRA_DOCUMENT) as Document?
            if (tempDocument != null) {
                this.document = tempDocument
                showInfo()
                showControls(isNewlyScanned = false, showRoleControls = true)
            }
        } else {
            val text = intent.extras?.getString(EXTRA_TEXT)
            val tempDocument = intent.extras?.getSerializable(EXTRA_DOCUMENT) as? Document
            val showRoleControls: Boolean = intent.extras?.getBoolean(EXTRA_CONTROLS) ?: false

            if (text != null) {
                analyzeText(text)
                showInfo()
            } else if (tempDocument != null) {
                this.document = tempDocument
                showInfo()
            }

            showControls(text != null, showRoleControls)

            viewModel.getDocumentsLiveData().observe(this) { task ->
                task.addOnSuccessListener { savedDocument ->
                    if (savedDocument.id.isNotBlank()) {
                        document.id = savedDocument.id
                        viewModel.updateDocument(document, DocumentAction.SCAN)
                        finish()
                    }
                }
                task.addOnFailureListener {
                    Toast.makeText(this, "An error occurred.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showControls(isNewlyScanned: Boolean, showRoleControls: Boolean) {
        if (isNewlyScanned) {
            binding.apply {
                dividerButtons.visibility = View.VISIBLE
                buttonSave.visibility = View.VISIBLE
                buttonSave.setOnClickListener {
                    document.documentStatus.scannedProperly = true
                    if (AuthenticationRepository.user!!.role == UserRole.REVISER) {
                        document.documentStatus.revised = true
                        document.documentByUser.revisedBy = AuthenticationRepository.user!!.id
                    }
                    viewModel.saveDocument(document)
                }
                buttonDiscard.visibility = View.VISIBLE
                buttonDiscard.setOnClickListener {
                    document.documentStatus.scannedProperly = false
                    if (AuthenticationRepository.user!!.role == UserRole.REVISER) {
                        document.documentStatus.revised = false
                    }
                    viewModel.saveDocument(document)
                }
            }
        } else if (showRoleControls) {
            val role = AuthenticationRepository.user!!.role
            when {
                role == UserRole.REVISER && !document.documentStatus.revised -> {
                    binding.apply {
                        dividerButtons.visibility = View.VISIBLE
                        buttonSave.apply {
                            visibility = View.VISIBLE
                            text = context.getString(R.string.revise_button)
                            setOnClickListener {
                                document.documentStatus.revised = true
                                document.documentByUser.revisedBy =
                                    AuthenticationRepository.user!!.id
                                viewModel.updateDocument(document, DocumentAction.REVISE)
                                HistoryFragment.removeWithId = document.id
                                finish()
                            }
                        }
                    }
                }
                role == UserRole.DIRECTOR && !document.documentStatus.signed -> {
                    binding.apply {
                        dividerButtons.visibility = View.VISIBLE
                        buttonSave.apply {
                            visibility = View.VISIBLE
                            text = context.getString(R.string.sign_button)
                            setOnClickListener {
                                document.documentStatus.signed = true
                                document.documentByUser.signedBy =
                                    AuthenticationRepository.user!!.id
                                viewModel.updateDocument(document, DocumentAction.SIGN)
                                HistoryFragment.removeWithId = document.id
                                finish()
                            }
                        }
                    }
                }
                role == UserRole.ACCOUNTANT -> {
                    binding.apply {
                        if (!document.documentStatus.archived) {
                            dividerButtons.visibility = View.VISIBLE
                            buttonDiscard.apply {
                                visibility = View.VISIBLE
                                text = context.getString(R.string.archive_button)
                                setOnClickListener {
                                    document.documentStatus.archived = true
                                    document.documentByUser.archivedBy =
                                        AuthenticationRepository.user!!.id
                                    viewModel.updateDocument(document, DocumentAction.ARCHIVE)
                                    HistoryFragment.removeWithId = document.id
                                    finish()
                                }
                            }
                        }
                        if (!document.documentStatus.signed) {
                            buttonSave.apply {
                                visibility = View.VISIBLE
                                text = context.getString(R.string.send_to_sign_button)
                                textSize = 14f
                                setOnClickListener {
                                    document.documentStatus.toBeSigned = true
                                    document.documentByUser.sentToSignBy =
                                        AuthenticationRepository.user!!.id
                                    viewModel.updateDocument(document, DocumentAction.SEND_TO_SIGN)
                                    HistoryFragment.removeWithId = document.id
                                    finish()
                                }
                            }
                        } else {
                            binding.dividerButtons.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showInfo() {
        binding.documentSignature.text = document.label
        when (document.type) {
            DocumentType.INTERNAL -> {
                binding.apply {
                    textContents.visibility = View.VISIBLE
                    textContents.text = (document as InternalDocument).content
                    items.visibility = View.GONE
                    totalLabel.visibility = View.GONE
                    clientName.visibility = View.GONE
                    divider.visibility = View.GONE
                    supportActionBar?.title = "INTERNAL DOCUMENT"
                }
            }
            DocumentType.OFFER -> {
                val offer = document as Offer
                binding.apply {
                    clientName.visibility = View.GONE
                    items.adapter = ItemsAdapter(offer.items)
                    items.layoutManager = LinearLayoutManager(
                        this@DocumentActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    totalValue.text = offer.totalPrice.toString()
                    supportActionBar?.title = "OFFER"
                }
            }
            DocumentType.RECEIPT -> {
                val receipt = document as Receipt
                binding.apply {
                    clientName.text = receipt.clientName
                    items.adapter = ItemsAdapter(receipt.items)
                    items.layoutManager = LinearLayoutManager(
                        this@DocumentActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    totalValue.text = receipt.totalPrice.toString()
                    supportActionBar?.title = "RECEIPT"
                }
            }
        }
    }

    private fun analyzeText(text: String) {
        val lines = text.split("\n")
            .map { line -> line.trim() }
            .filter { line -> line.isNotEmpty() && !line.startsWith("--") }

        val signature = lines.first()
        when {
            lines.first() matches receiptRegex -> {
                supportActionBar?.title = "RECEIPT"
                document = Receipt(
                    "",
                    System.currentTimeMillis() / 1000,
                    signature,
                    DocumentStatus(),
                    DocumentByUser(),
                    DocumentType.RECEIPT,
                    "",
                    HashMap(),
                    0.0
                )
                analyzeReceiptType(lines, true)
            }
            lines.first() matches offerRegex -> {
                supportActionBar?.title = "OFFER"
                document = Offer(
                    "",
                    System.currentTimeMillis() / 1000,
                    signature,
                    DocumentStatus(),
                    DocumentByUser(),
                    DocumentType.OFFER,
                    HashMap(),
                    0.0
                )
                analyzeReceiptType(lines)
            }
            lines.first() matches internalDocumentRegex -> {
                supportActionBar?.title = "INTERNAL DOCUMENT"
                val rawText = lines.takeLast(lines.size - 1).joinToString(" ")
                document = InternalDocument(
                    "",
                    System.currentTimeMillis() / 1000,
                    signature,
                    DocumentStatus(),
                    DocumentByUser(),
                    DocumentType.INTERNAL,
                    rawText
                )
            }
            else -> {
                Toast.makeText(this, "Please scan again.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun analyzeReceiptType(items: List<String>, isReceipt: Boolean = false) {
        val itemName = mutableListOf<String>()
        val itemPrice = mutableListOf<String>()
        val tempItems = items.toMutableList()

        if (tempItems.isEmpty()) {
            Toast.makeText(this, "Please scan again.", Toast.LENGTH_SHORT).show()
            finish()
        }

        tempItems.remove(tempItems.first())
        val total = tempItems.find { it.startsWith("UKUPNO", ignoreCase = true) }?.let {
            it.split(":")[1].trim()
        }
        tempItems.remove(total)

        var clientName = ""
        if (isReceipt) {
            clientName = tempItems.removeFirst()
        }

        tempItems.forEach { item ->
            if (item.isDigitsOnly()) itemPrice.add(item) else itemName.add(item)
        }

        val itemPriceMap = mutableMapOf<String, Double>()
        for (i in 0 until min(itemName.size, itemPrice.size)) {
            val price = itemPrice[i].removePrefix("-").trim().toDouble()
            itemPriceMap[itemName[i]] = price
        }

        if (isReceipt) {
            (document as Receipt).apply {
                this.clientName = clientName
                this.totalPrice = total?.toDouble() ?: 0.0
                this.items = itemPriceMap
            }
        } else {
            (document as Offer).apply {
                this.totalPrice = total?.toDouble() ?: 0.0
                this.items = itemPriceMap
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {

        val receiptRegex = Regex("R\\d{6}")
        val offerRegex = Regex("P\\d{9}")
        val internalDocumentRegex = Regex("INT\\d{4}")

        fun start(context: Context, document: Document, showControls: Boolean = false) {
            Intent(context, DocumentActivity::class.java)
                .apply {
                    putExtra(EXTRA_DOCUMENT, document)
                    putExtra(EXTRA_DOCUMENT, document)
                    putExtra(EXTRA_CONTROLS, showControls)
                }
                .also {
                    context.startActivity(it)
                }
        }

        @JvmStatic
        fun start(context: Context, text: String) {
            Intent(context, DocumentActivity::class.java)
                .apply {
                    putExtra(EXTRA_TEXT, text)
                }
                .also {
                    context.startActivity(it)
                }
        }
    }
}
