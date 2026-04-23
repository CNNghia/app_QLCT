package com.app.qlct

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

data class FaqItem(val question: String, val answer: String, var isExpanded: Boolean = false)

class FaqActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarFaq)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        val rvFaq = findViewById<RecyclerView>(R.id.rvFaq)
        
        // Danh sách các câu hỏi thường gặp giả định mượt mà liên quan tới App
        val faqList = listOf(
            FaqItem(
                "Làm thế nào để ghi nhận một khoản chi tiêu mới?",
                "Rất đơn giản! Bạn chỉ cần nhấn vào biểu tượng dấu (+) trên giao diện của bất kỳ màn hình nào (Tổng quan, hoặc Lịch), chọn tab 'CHI PHÍ', nhập số tiền và bấm lưu."
            ),
            FaqItem(
                "Làm sao để quản lý các ví riêng biệt (như Ví Momo, Tiền mặt)?",
                "Bạn có thể mở Menubar -> Chọn 'Các tài khoản (Ví)'. Ở đây, hệ thống cho phép bạn tạo ra các ví nhỏ lẻ khác nhau với tên và số dư đi kèm. Khi nhập giao dịch, máy sẽ hỏi bạn trừ/cộng tiền từ Ví nào."
            ),
            FaqItem(
                "Tôi nhập sai giao dịch thì có xoá hoặc sửa được không?",
                "Hoàn toàn được! Bạn hãy vào trang Danh sách giao dịch, có thể Nhấn Giữ vào giao dịch để Xoá, hoặc Bấm 1 lần vào nó để chỉnh sửa lại toàn bộ thông tin."
            ),
            FaqItem(
                "Tại sao con số Chênh Lệch trên biểu đồ lại báo Đỏ (Âm)?",
                "Khi tổng các khoản Chi Phí của bạn vượt quá số tiền Thu Nhập nhận được trong tháng đó, con số lợi nhuận sẽ âm. Theo triết lý quản lý tài chính, đây là lời cảnh báo rủi ro bạo chi."
            ),
            FaqItem(
                "Tính năng Thống kê Sơ lược khác Lịch cụ thể ở chỗ nào?",
                "Trang 'Sơ lược' sẽ nhóm tất cả thông tin lại cung cấp đánh giá bức tranh tài chính chung của cả tháng/năm. Trong khi đó mục 'Lịch' giúp rà soát chi tiết số tiền chi tiêu từng ngày, bảo vệ tài chính chặt chẽ hơn."
            ),
            FaqItem(
                "Mọi dữ liệu của tôi có được lưu trữ an toàn không?",
                "Toàn bộ dữ liệu của bạn chỉ được lưu trên cơ sở dữ liệu nội bộ (Local Database/Room) của thiết bị này, hoàn toàn không bị rò rỉ ra ngoài internet."
            )
        )

        val adapter = FaqAdapter(faqList)
        rvFaq.adapter = adapter
    }

    inner class FaqAdapter(private val items: List<FaqItem>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

        inner class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
            val tvAnswer: TextView = itemView.findViewById(R.id.tvAnswer)
            val ivExpand: ImageView = itemView.findViewById(R.id.ivExpand)

            fun bind(item: FaqItem) {
                tvQuestion.text = item.question
                tvAnswer.text = item.answer
                
                // Mở rộng hoặc thu gọn đáp án
                tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                ivExpand.rotation = if (item.isExpanded) 180f else 0f

                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
                    TransitionManager.beginDelayedTransition(itemView.parent as ViewGroup, AutoTransition())
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(pos)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
            return FaqViewHolder(view)
        }

        override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }
}
