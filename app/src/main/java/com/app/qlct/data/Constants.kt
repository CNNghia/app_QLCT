package com.app.qlct.data

/**
 * Hằng số tập trung cho toàn app.
 * Thay thế magic string "INCOME"/"EXPENSE" rải rác ở nhiều file.
 */
object TransactionType {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
}

object AppConstants {
    const val CURRENCY_SYMBOL = "đ"
    const val DEFAULT_WALLET_NAME = "Ví Tiền Mặt"
    const val DEFAULT_CURRENCY = "đ"
    const val DB_NAME = "expense_manager_db"
}
