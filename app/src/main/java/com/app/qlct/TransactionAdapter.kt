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

// Anh: Adapter này đóng vai trò cầu nối, nhận dữ liệu danh sách Giao dịch từ Database và "vẽ" chúng lên màn hình (RecyclerView)
class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit, // Anh: Hàm callback khi người dùng bấm vào 1 dòng (để sửa)
    private val onItemLongClick: (Transaction) -> Unit // Anh: Hàm callback khi người dùng nhấn giữ (để xóa)
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback()) {

    // Anh: Lớp ViewHolder dùng để ánh xạ các view (TextView) từ file giao diện item_transaction.xml
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
        
        // Anh: Gán sự kiện click cho từng dòng giao dịch
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
            true
        }

        // Anh: Nếu có ghi chú thì hiện ghi chú, không có thì lấy tên Danh mục (VD: Ăn uống) làm tên chính
        holder.tvNote.text = if (item.note.isNotEmpty()) item.note else item.categoryName
        
        // Anh: Định dạng ngày tháng năm để hiển thị đẹp mắt
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = "${sdf.format(Date(item.date))} • ${item.categoryName}"
        
        // Anh: Định dạng lại số tiền thành chuỗi có dấu phẩy (VD: 50,000)
        val df = DecimalFormat("#,###")
        val amountFormatted = df.format(item.amount)
        
        // Anh: Nếu là THU (INCOME) thì in chữ Xanh lá và thêm dấu +, CHI (EXPENSE) thì màu Đỏ và dấu -
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
