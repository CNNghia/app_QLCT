package com.app.qlct

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.qlct.ui.theme.App_QLCTTheme
import com.app.qlct.ui.wallet.WalletScreen

class WalletActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App_QLCTTheme {
                WalletScreen()
            }
        }
    }
}
