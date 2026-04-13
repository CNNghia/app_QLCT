package com.app.qlct.ui.wallet

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.qlct.model.Wallet
import com.app.qlct.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình chính quản lý ví.
 * Hiển thị tổng số dư, danh sách ví, và cho phép thêm / sửa / xóa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = viewModel()
) {
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()

    // Trạng thái dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var walletToEdit by remember { mutableStateOf<Wallet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quản lý ví",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm ví mới",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Banner tổng số dư ──────────────────────────────────────
            TotalBalanceBanner(totalBalance = totalBalance)

            // ── Danh sách ví ──────────────────────────────────────────
            if (wallets.isEmpty()) {
                EmptyWalletPlaceholder(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = wallets,
                        key = { it.id }
                    ) { wallet ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                        ) {
                            WalletCard(
                                wallet = wallet,
                                onEdit = { walletToEdit = it },
                                onDelete = { viewModel.deleteWallet(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Thêm ví
    if (showAddDialog) {
        AddEditWalletDialog(
            wallet = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, balance, currency ->
                viewModel.addWallet(name, balance, currency)
                showAddDialog = false
            }
        )
    }

    // Dialog Sửa ví
    walletToEdit?.let { wallet ->
        AddEditWalletDialog(
            wallet = wallet,
            onDismiss = { walletToEdit = null },
            onConfirm = { name, balance, currency ->
                viewModel.updateWallet(
                    wallet.copy(name = name, balance = balance, currency = currency)
                )
                walletToEdit = null
            }
        )
    }
}

// ── Sub-composables ────────────────────────────────────────────────────────────

@Composable
private fun TotalBalanceBanner(totalBalance: Double) {
    val formatted = remember(totalBalance) {
        val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        format.format(totalBalance) + " VND"
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tổng số dư",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatted,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyWalletPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có ví nào",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Nhấn + để thêm ví mới",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
