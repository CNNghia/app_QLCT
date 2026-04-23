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
    onDelete: (Wallet) -> Unit,   // xóa trực tiếp (legacy, giữ lại để tương thích)
    onDeleteSafe: ((Wallet) -> Unit)? = null,   // xóa có kiểm tra transaction
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
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thông tin ví
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formattedBalance,
                    style = MaterialTheme.typography.bodyLarge,
                    color = balanceColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Nút Sửa (Xanh nhạt/Cyan)
            IconButton(
                onClick = { onEdit(wallet) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sửa ví",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            // Nút Xóa (Đỏ thẫm)
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa ví",
                    tint = Color(0xFFC62828), // Dark red
                    modifier = Modifier.size(20.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
