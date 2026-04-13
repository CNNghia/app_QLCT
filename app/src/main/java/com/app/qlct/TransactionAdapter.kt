package com.app.qlct

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.qlct.data.entity.Transaction
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNote: TextView = view.findViewById(R.id.tvTransactionNote)
        val tvDate: TextView = view.findViewById(R.id.tvTransactionDate)
        val tvAmount: TextView = view.findViewById(R.id.tvTransactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        
        holder.tvNote.text = if (item.note.isNotEmpty()) item.note else item.categoryName
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = "${sdf.format(Date(item.date))} • ${item.categoryName}"
        
        val df = DecimalFormat("#,###")
        val amountFormatted = df.format(item.amount)
        
        if (item.type == "INCOME") {
            holder.tvAmount.text = "+ $amountFormatted đ"
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvAmount.text = "- $amountFormatted đ"
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
