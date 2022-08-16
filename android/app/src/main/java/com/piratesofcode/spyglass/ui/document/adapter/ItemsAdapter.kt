package com.piratesofcode.spyglass.ui.document.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.piratesofcode.spyglass.databinding.LayoutReceiptItemBinding

class ItemsAdapter(private var items: Map<String, Double> = mutableMapOf()) :
    RecyclerView.Adapter<ItemsAdapter.ReceiptItemViewHolder>() {

    inner class ReceiptItemViewHolder(private val binding: LayoutReceiptItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Map.Entry<String, Double>) {
            binding.apply {
                binding.name.text = item.key
                binding.price.text = item.value.toString()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemsAdapter.ReceiptItemViewHolder {
        return ReceiptItemViewHolder(
            LayoutReceiptItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemsAdapter.ReceiptItemViewHolder, position: Int) {
        holder.bind(items.entries.toList()[position])
    }

    override fun getItemCount() = items.size
}
