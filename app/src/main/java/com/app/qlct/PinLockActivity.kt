package com.app.qlct

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinLockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("AppConfig", MODE_PRIVATE)
        val isLocked = prefs.getBoolean("PIN_LOCKED", false)
        val savedPin = prefs.getString("APP_PIN", "") ?: ""

        // Nếu người dùng chưa từng bật PIN Lock hoặc chưa có Mã PIN thì Bỏ qua khóa và Mở thẳng App luôn
        if (!isLocked || savedPin.isEmpty()) {
            openMainApp()
            return
        }

        // Bật màn hình Két sắt lên nếu đúng là đang bị khóa
        setContentView(R.layout.activity_pin_lock)

        val etPin = findViewById<EditText>(R.id.etPin)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)

        btnUnlock.setOnClickListener {
            val inputPin = etPin.text.toString()
            if (inputPin == savedPin) {
                // Mã PIN chính xác, vượt rào màn hình này
                openMainApp()
            } else {
                Toast.makeText(this, "Sai mã PIN! Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                etPin.text.clear()
            }
        }
    }

    private fun openMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Tiêu hủy màn hình Khóa để tránh dùng phím Back quay lại đây
    }
}
