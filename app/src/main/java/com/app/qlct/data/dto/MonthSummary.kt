package com.app.qlct.data.dto

data class MonthSummary(
    val year: Int,
    val month: Int,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val balance: Double get() = totalIncome - totalExpense
}
