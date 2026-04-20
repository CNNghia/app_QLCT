package com.app.qlct

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.CategoryRepository
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.data.entity.Transaction
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory
import java.text.DecimalFormat
import java.util.Calendar

data class CalendarDay(
    val day: Int,
    val month: Int,
    val year: Int,
    var income: Double = 0.0,
    var expense: Double = 0.0,
    val isPadding: Boolean = false,
    var isSelected: Boolean = false
) {
    val net get() = income - expense
}

class CalendarActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { WalletRepository(database.walletDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository, categoryRepository)
    }

    private var currentMonthCal = Calendar.getInstance()
    private var selectedDateCal = Calendar.getInstance()
    private var allTrans: List<Transaction> = emptyList()

    private val calendarDays = mutableListOf<CalendarDay>()
    private lateinit var calAdapter: CalAdapter
    private lateinit var dailyTxAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarCal).apply {
            setNavigationIcon(android.R.drawable.ic_menu_revert)
            setNavigationOnClickListener { finish() }
        }

        val rvCalendar = findViewById<RecyclerView>(R.id.rvCalendar)
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        calAdapter = CalAdapter()
        rvCalendar.adapter = calAdapter

        val rvDailyTrans = findViewById<RecyclerView>(R.id.rvDailyTrans)
        rvDailyTrans.layoutManager = LinearLayoutManager(this)
        dailyTxAdapter = TransactionAdapter(
            onItemClick = { transaction ->
                val intent = Intent(this, AddTransactionActivity::class.java).apply {
                    putExtra("TRANSACTION_ID", transaction.id)
                    putExtra("AMOUNT", transaction.amount)
                    putExtra("NOTE", transaction.note)
                    putExtra("CATEGORY", transaction.categoryName)
                    putExtra("WALLET", transaction.walletName)
                    putExtra("DATE", transaction.date)
                    putExtra("TYPE", transaction.type)
                }
                startActivity(intent)
            },
            onItemLongClick = {}
        )
        rvDailyTrans.adapter = dailyTxAdapter

        findViewById<ImageButton>(R.id.btnPrevMonthCal).setOnClickListener {
            currentMonthCal.add(Calendar.MONTH, -1)
            refreshCalendar()
        }
        findViewById<ImageButton>(R.id.btnNextMonthCal).setOnClickListener {
            currentMonthCal.add(Calendar.MONTH, 1)
            refreshCalendar()
        }

        findViewById<View>(R.id.fabAddCal).setOnClickListener {
            val tc = Calendar.getInstance()
            // Optional: Pass selected date to AddTransactionActivity to preset the date picker
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        viewModel.allTransactions.observe(this) { transactions ->
            if (transactions != null) {
                allTrans = transactions
                refreshCalendar()
            }
        }
    }

    private fun refreshCalendar() {
        val monthNames = arrayOf("Một", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Mười Một", "Mười Hai")
        findViewById<TextView>(R.id.tvMonthYearCal).text = "Tháng ${monthNames[currentMonthCal.get(Calendar.MONTH)]} ${currentMonthCal.get(Calendar.YEAR)}"

        calendarDays.clear()

        val cal = currentMonthCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        var startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 2
        if (startDayOfWeek < 0) startDayOfWeek += 7

        val prevMonthCal = cal.clone() as Calendar
        prevMonthCal.add(Calendar.MONTH, -1)
        val prevMonthDays = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in startDayOfWeek - 1 downTo 0) {
            calendarDays.add(CalendarDay(prevMonthDays - i, prevMonthCal.get(Calendar.MONTH), prevMonthCal.get(Calendar.YEAR), isPadding = true))
        }

        val amountOfDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val cy = cal.get(Calendar.YEAR)
        val cm = cal.get(Calendar.MONTH)
        for (i in 1..amountOfDays) {
            val isSel = (cy == selectedDateCal.get(Calendar.YEAR) && cm == selectedDateCal.get(Calendar.MONTH) && i == selectedDateCal.get(Calendar.DAY_OF_MONTH))
            calendarDays.add(CalendarDay(i, cm, cy, isSelected = isSel))
        }

        val incomeMap = mutableMapOf<String, Double>()
        val expMap = mutableMapOf<String, Double>()
        
        allTrans.forEach { t ->
            val tc = Calendar.getInstance()
            tc.timeInMillis = t.date
            val key = "${tc.get(Calendar.YEAR)}-${tc.get(Calendar.MONTH)}-${tc.get(Calendar.DAY_OF_MONTH)}"
            if (t.type == "INCOME") {
                incomeMap[key] = (incomeMap[key] ?: 0.0) + t.amount
            } else {
                expMap[key] = (expMap[key] ?: 0.0) + t.amount
            }
        }

        calendarDays.filter { !it.isPadding }.forEach { cd ->
            val key = "${cd.year}-${cd.month}-${cd.day}"
            cd.income = incomeMap[key] ?: 0.0
            cd.expense = expMap[key] ?: 0.0
        }

        calAdapter.notifyDataSetChanged()
        updateBottomSummary()
    }

    private fun updateBottomSummary() {
        val selDay = calendarDays.find { it.isSelected }
        val tvSelDate = findViewById<TextView>(R.id.tvSelDate)
        val tvSelNet = findViewById<TextView>(R.id.tvSelNet)
        val tvSelInc = findViewById<TextView>(R.id.tvSelInc)
        val tvSelExp = findViewById<TextView>(R.id.tvSelExp)
        val tvEmptyDaily = findViewById<TextView>(R.id.tvEmptyDaily)
        val rvDailyTrans = findViewById<RecyclerView>(R.id.rvDailyTrans)

        if (selDay == null) return

        val dayNames = arrayOf("CN", "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7")
        val c = Calendar.getInstance()
        c.set(selDay.year, selDay.month, selDay.day)
        tvSelDate.text = "${dayNames[c.get(Calendar.DAY_OF_WEEK) - 1]} ${String.format("%02d/%02d/%04d", selDay.day, selDay.month + 1, selDay.year)}"

        val df = DecimalFormat("#,###")
        tvSelNet.text = df.format(selDay.net).replace(",", ".") + " đ"
        tvSelInc.text = df.format(selDay.income).replace(",", ".") + " đ"
        tvSelExp.text = df.format(selDay.expense).replace(",", ".") + " đ"

        val dailyTx = allTrans.filter {
            val tc = Calendar.getInstance()
            tc.timeInMillis = it.date
            tc.get(Calendar.YEAR) == selDay.year && tc.get(Calendar.MONTH) == selDay.month && tc.get(Calendar.DAY_OF_MONTH) == selDay.day
        }
        
        dailyTxAdapter.submitList(dailyTx)

        if (dailyTx.isEmpty()) {
            rvDailyTrans.visibility = View.GONE
            tvEmptyDaily.visibility = View.VISIBLE
        } else {
            rvDailyTrans.visibility = View.VISIBLE
            tvEmptyDaily.visibility = View.GONE
        }
    }

    inner class CalAdapter : RecyclerView.Adapter<CalAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvDayNum = v.findViewById<TextView>(R.id.tvDayNum)
            val tvDayNet = v.findViewById<TextView>(R.id.tvDayNet)

            fun bind(cd: CalendarDay) {
                tvDayNum.text = cd.day.toString()
                if (cd.isPadding) {
                    tvDayNum.setTextColor(Color.parseColor("#E0E0E0"))
                    tvDayNet.visibility = View.INVISIBLE
                    itemView.setOnClickListener(null)
                    val bg = GradientDrawable()
                    bg.setColor(Color.TRANSPARENT)
                    tvDayNum.background = bg
                } else {
                    tvDayNum.setTextColor(if (cd.isSelected) Color.WHITE else Color.BLACK)
                    val bg = GradientDrawable()
                    bg.shape = GradientDrawable.OVAL
                    bg.setColor(if (cd.isSelected) Color.parseColor("#D4E6F1") else Color.TRANSPARENT)
                    // Customize text color logic specifically for selected to match image 
                    if(cd.isSelected) tvDayNum.setTextColor(Color.BLACK)
                    
                    tvDayNum.background = bg

                    if (cd.net != 0.0) {
                        tvDayNet.visibility = View.VISIBLE
                        val prefix = if (cd.net > 0) "+" else ""
                        val colorStr = if (cd.net > 0) "#4CAF50" else "#F44336"
                        tvDayNet.setTextColor(Color.parseColor(colorStr))
                        
                        val df = DecimalFormat("#,###")
                        tvDayNet.text = prefix + df.format(cd.net).replace(",", ".")
                    } else {
                        tvDayNet.visibility = View.INVISIBLE
                    }

                    itemView.setOnClickListener {
                        calendarDays.forEach { it.isSelected = false }
                        cd.isSelected = true
                        selectedDateCal.set(cd.year, cd.month, cd.day)
                        notifyDataSetChanged()
                        updateBottomSummary()
                    }
                }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(calendarDays[position])
        override fun getItemCount() = calendarDays.size
    }
}
