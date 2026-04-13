package com.app.qlct.ui.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.qlct.model.Wallet

/**
 * Dialog dùng để thêm mới hoặc chỉnh sửa một ví.
 * Nếu [wallet] != null → đang ở chế độ sửa.
 */
@Composable
fun AddEditWalletDialog(
    wallet: Wallet? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, balance: Double, currency: String) -> Unit
) {
    var name by remember { mutableStateOf(wallet?.name ?: "") }
    var balanceText by remember { mutableStateOf(wallet?.balance?.toString() ?: "") }
    var currency by remember { mutableStateOf(wallet?.currency ?: "VND") }

    var nameError by remember { mutableStateOf(false) }
    var balanceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (wallet == null) "Thêm ví mới" else "Chỉnh sửa ví",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tên ví
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Tên ví") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("Tên ví không được để trống")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Số dư ban đầu
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = {
                        balanceText = it
                        balanceError = false
                    },
                    label = { Text("Số dư") },
                    isError = balanceError,
                    supportingText = {
                        if (balanceError) Text("Số dư không hợp lệ")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Đơn vị tiền tệ
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Đơn vị tiền tệ") },
                    placeholder = { Text("VND, USD, EUR...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var valid = true
                    if (name.isBlank()) {
                        nameError = true
                        valid = false
                    }
                    val balance = balanceText.toDoubleOrNull()
                    if (balance == null) {
                        balanceError = true
                        valid = false
                    }
                    if (valid && balance != null) {
                        onConfirm(name, balance, currency)
                    }
                }
            ) {
                Text(if (wallet == null) "Thêm" else "Lưu")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
