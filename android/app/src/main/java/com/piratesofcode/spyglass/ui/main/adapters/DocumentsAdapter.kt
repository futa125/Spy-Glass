package com.piratesofcode.spyglass.ui.main.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.piratesofcode.spyglass.databinding.ScannedDocumentBinding
import com.piratesofcode.spyglass.model.document.Document
import com.piratesofcode.spyglass.model.document.Offer
import com.piratesofcode.spyglass.model.document.Receipt
import com.piratesofcode.spyglass.ui.document.DocumentActivity

class DocumentsAdapter(val context: Context, private var documents: List<Document> = emptyList()) :
    RecyclerView.Adapter<DocumentsAdapter.DocumentsViewHolder>() {

    var showRoleControls = false

    inner class DocumentsViewHolder(private val binding: ScannedDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(document: Document) {
            binding.apply {
                title.text = document.type.toString()
                code.text = document.label
                status.text = document.documentStatus.toString()

                when (document) {
                    is Receipt -> {
                        clientName.visibility = View.VISIBLE
                        total.visibility = View.VISIBLE
                        clientName.text = document.clientName
                        total.text = document.totalPrice.toString()
                    }
                    is Offer -> {
                        clientName.visibility = View.GONE
                        total.visibility = View.VISIBLE
                        total.text = document.totalPrice.toString()
                    }
                    else -> {
                        clientName.visibility = View.GONE
                        total.visibility = View.GONE
                    }
                }

                scannedDocument.setOnClickListener {
                    DocumentActivity.start(context, document, showRoleControls)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentsViewHolder {
        return DocumentsViewHolder(
            ScannedDocumentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DocumentsViewHolder, position: Int) {
        holder.bind(documents[position])
    }

    override fun getItemCount() = documents.size

    @SuppressLint("NotifyDataSetChanged")
    fun setDocuments(items: List<Document>) {
        this.documents = items.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }

    fun removeItem(removeWithId: String) {
        val remove = documents.find { it.id == removeWithId }
        val newItems = ArrayList(documents)
        newItems.remove(remove)
        setDocuments(newItems)
    }
}
