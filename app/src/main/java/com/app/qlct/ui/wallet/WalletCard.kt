package com.app.qlct.ui.wallet

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.qlct.model.Wallet
import java.text.NumberFormat
import java.util.Locale

/**
 * Card hiển thị thông tin một ví: tên, số dư, đơn vị tiền tệ.
 * Cung cấp nút Sửa và Xóa.
 */
@Composable
fun WalletCard(
    wallet: Wallet,
    onEdit: (Wallet) -> Unit,
    onDelete: (Wallet) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Format số dư theo định dạng có dấu phân cách hàng nghìn
    val formattedBalance = remember(wallet.balance, wallet.currency) {
        val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        "${format.format(wallet.balance)} ${wallet.currency}"
    }

    // Màu số dư: xanh nếu dương, đỏ nếu âm
    val balanceColor = if (wallet.balance >= 0)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thông tin ví
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedBalance,
                    style = MaterialTheme.typography.bodyLarge,
                    color = balanceColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Nút Sửa
            IconButton(onClick = { onEdit(wallet) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sửa ví ${wallet.name}",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // Nút Xóa
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa ví ${wallet.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Dialog xác nhận xóa
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xóa ví") },
            text = { Text("Bạn có chắc muốn xóa ví \"${wallet.name}\" không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(wallet)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}
