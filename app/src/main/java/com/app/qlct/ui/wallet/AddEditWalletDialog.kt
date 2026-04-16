package com.app.qlct.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.app.qlct.model.Wallet

class MoneyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formattedText = try {
            val amount = originalText.toLong()
            "%,d".format(amount).replace(",", ".")
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formattedText.length
            override fun transformedToOriginal(offset: Int): Int = originalText.length
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

@Composable
fun AddEditWalletDialog(
    wallet: Wallet? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, balance: Double, currency: String) -> Unit
) {
    var name by remember { mutableStateOf(wallet?.name ?: "") }
    var balanceText by remember { mutableStateOf(if (wallet != null) wallet.balance.toLong().toString() else "") }
    var isInitializedBalance by remember { mutableStateOf(wallet != null) }
    
    val currencies = listOf("đ", "$", "€", "¥")
    val defaultCurr = if (wallet?.currency != null) wallet.currency else "đ"
    var currency by remember { mutableStateOf(defaultCurr) }
    var expandedCurrency by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var balanceError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = if (wallet == null) "TẠO TÀI KHOẢN MỚI" else "CHỈNH SỬA TÀI KHOẢN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val focusedLine = MaterialTheme.colorScheme.primary
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tên:",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextField(
                            value = name,
                            onValueChange = { name = it; nameError = false },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Gray,
                                focusedIndicatorColor = focusedLine
                            ),
                            singleLine = true,
                            isError = nameError,
                            modifier = Modifier.weight(2f),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Đơn vị tiền tệ của tài\nkhoản:",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            TextField(
                                value = currency,
                                onValueChange = {},
                                readOnly = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF0F6FF),
                                    unfocusedContainerColor = Color(0xFFF0F6FF),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                            )
                            // Lớp che để hứng sự kiện Click
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { expandedCurrency = true }
                            )
                            DropdownMenu(
                                expanded = expandedCurrency,
                                onDismissRequest = { expandedCurrency = false },
                                modifier = Modifier.fillMaxWidth(0.4f)
                            ) {
                                currencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                        onClick = { 
                                            currency = curr
                                            expandedCurrency = false 
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Số tiền:",
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.8f)
                        ) {
                            TextField(
                                value = balanceText,
                                onValueChange = { input ->
                                    val raw = input.filter { it.isDigit() }
                                    balanceText = raw
                                    balanceError = false
                                },
                                visualTransformation = MoneyVisualTransformation(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Gray,
                                    focusedIndicatorColor = focusedLine
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = balanceError,
                                placeholder = {
                                    Text(
                                        text = "0",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        style = LocalTextStyle.current
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = "",
                        onValueChange = { },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Gray,
                            focusedIndicatorColor = focusedLine
                        ),
                        placeholder = { Text("Ghi chú (Không bắt buộc)", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.primary),
                        shape = RectangleShape
                    ) {
                        Text("HỦY", color = Color.White)
                    }
                    TextButton(
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
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.primary),
                        shape = RectangleShape
                    ) {
                        Text("LƯU LẠI", color = Color.White)
                    }
                }
            }
        }
    }
}
