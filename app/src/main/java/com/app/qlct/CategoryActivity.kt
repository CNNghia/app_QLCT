package com.app.qlct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.CategoryRepository
import com.app.qlct.data.entity.Category
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }

    private lateinit var rvCategory: RecyclerView
    private lateinit var adapter: CatAdapter
    private var currentType = "INCOME"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarCategory).apply {
            setNavigationIcon(android.R.drawable.ic_menu_revert)
            setNavigationOnClickListener { finish() }
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutCategory)
        rvCategory = findViewById(R.id.rvCategory)
        rvCategory.layoutManager = LinearLayoutManager(this)
        
        adapter = CatAdapter(emptyList()) { cat ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xoá danh mục")
                .setMessage("Bạn có chắc muốn xoá danh mục '${cat.name}' không? Sẽ không thể phục hồi.")
                .setPositiveButton("Xoá") { _, _ ->
                    lifecycleScope.launch {
                        categoryRepository.deleteCategory(cat)
                        Toast.makeText(this@CategoryActivity, "Đã xóa ${cat.name}", Toast.LENGTH_SHORT).show()
                        // Dữ liệu sẽ tự load lại nhờ Flow nếu được setup đúng, 
                        // nhưng để chắc chắn ta load thủ công luôn.
                        loadCategories() 
                    }
                }
                .setNegativeButton("Huỷ", null)
                .show()
        }
        rvCategory.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentType = if (tab?.position == 0) "INCOME" else "EXPENSE"
                loadCategories()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        findViewById<View>(R.id.fabAddCategory).setOnClickListener {
            showAddCategoryDialog()
        }

        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            categoryRepository.getCategoriesByType(currentType).collect { list ->
                adapter.updateData(list)
            }
        }
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "Ví dụ: Lương, Tiền điện, Cà phê..."
        val layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = layoutParams
        
        val container = android.widget.LinearLayout(this)
        container.setPadding(50, 20, 50, 0)
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle(if (currentType == "INCOME") "Thêm Danh mục Thu nhập" else "Thêm Danh mục Chi tiêu")
            .setView(container)
            .setPositiveButton("Thêm") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val newCat = Category(name = name, type = currentType, icon = "ic_default")
                        categoryRepository.insertCategory(newCat)
                        Toast.makeText(this@CategoryActivity, "Đã thêm '$name'", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    inner class CatAdapter(private var list: List<Category>, private val onDeleteClick: (Category) -> Unit) : RecyclerView.Adapter<CatAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName = v.findViewById<TextView>(R.id.tvCatName)
            val btnDelete = v.findViewById<ImageView>(R.id.btnDeleteCat)
            val ivIcon = v.findViewById<ImageView>(R.id.ivCatIcon)
            fun bind(cat: Category) {
                tvName.text = cat.name
                ivIcon.setColorFilter(if (cat.type == "INCOME") android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.parseColor("#F44336"))
                btnDelete.setOnClickListener { onDeleteClick(cat) }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(list[position])
        override fun getItemCount() = list.size
        fun updateData(newList: List<Category>) {
            list = newList
            notifyDataSetChanged()
        }
    }
}
