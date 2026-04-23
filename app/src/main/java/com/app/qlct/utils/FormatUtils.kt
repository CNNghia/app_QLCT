package com.app.qlct.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions tiện ích dùng chung toàn app.
 * Thay thế pattern `DecimalFormat("#,###").format(x).replace(",", ".")` lặp lại ở 6 file.
 */

/** Format số tiền VNĐ: 1500000.0 → "1.500.000 đ" */
fun Double.formatVND(): String {
    val df = DecimalFormat("#,###")
    return df.format(this).replace(",", ".") + " đ"
}

/** Format số tiền VNĐ có dấu + hoặc - */
fun Double.formatVNDSigned(): String {
    val df = DecimalFormat("#,###")
    val formatted = df.format(kotlin.math.abs(this)).replace(",", ".")
    return when {
        this > 0 -> "+ $formatted đ"
        this < 0 -> "- $formatted đ"
        else -> "0 đ"
    }
}

/** Format timestamp thành chuỗi ngày: "23/04/2026" */
fun Long.formatDate(pattern: String = "dd/MM/yyyy"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

/** Format timestamp thành chuỗi ngày giờ: "23/04/2026 07:30" */
fun Long.formatDateTime(): String = this.formatDate("dd/MM/yyyy HH:mm")
