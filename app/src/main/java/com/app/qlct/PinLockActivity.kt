package com.app.qlct

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class PinLockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("AppConfig", MODE_PRIVATE)
        val isLocked = prefs.getBoolean("PIN_LOCKED", false)
        val isBioLocked = prefs.getBoolean("BIO_LOCKED", false)
        val savedPin = prefs.getString("APP_PIN", "") ?: ""

        // Tính năng: Vừa không khóa mã PIN và cũng không khóa sinh trắc học -> Vào thẳng
        if (!isLocked && !isBioLocked) {
            openMainApp()
            return
        }

        // Tạo màn hình nhập PIN cho cả 2 trường hợp (hoặc làm nền phía dưới)
        setContentView(R.layout.activity_pin_lock)

        val btnRepromptBio = findViewById<android.widget.TextView>(R.id.btnRepromptBio)

        // Gọi Sinh trắc học bằng cách hoãn lại 300ms để điện thoại load xong hoạt ảnh mở App, tránh lỗi văng Canceled do Cold-boot
        if (isBioLocked) {
            btnRepromptBio.visibility = android.view.View.VISIBLE
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                triggerBiometricLogin(isLocked)
            }, 500)
            
            btnRepromptBio.setOnClickListener {
                triggerBiometricLogin(isLocked)
            }
        }

        // Nếu người dùng KHÔNG dùng mã PIN, giấu cái thẻ trắng nhập PIN đi nhưng giữ lại nút quét Vân tay
        val etPin = findViewById<EditText>(R.id.etPin)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)
        val cardPin = findViewById<android.view.View>(R.id.etPin).parent.parent as android.view.View
        
        if (!isLocked) {
            etPin.visibility = android.view.View.GONE
            btnUnlock.visibility = android.view.View.GONE
        }

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
    private fun triggerBiometricLogin(hasPinFallback: Boolean) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Xác minh sinh trắc học thành công!", Toast.LENGTH_SHORT).show()
                    openMainApp()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Khuôn mặt/Vân tay không khớp!", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Người dùng bấm Hủy (Cancel) hoặc Lỗi không có cảm biến
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(applicationContext, "Hủy xác minh. $errString", Toast.LENGTH_SHORT).show()
                    }
                    if (!hasPinFallback) {
                        // Nếu không có PIN dự phòng mà Hủy sinh trắc -> Ném ra ngoài luôn không cho vào App
                        finish()
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Bảo mật Sinh Trắc Học")
            .setSubtitle("Vui lòng chạm Cảm biến vân tay hoặc Nhìn vào Camera")
            .setNegativeButtonText(if (hasPinFallback) "Sử dụng Mã PIN dự phòng" else "Hủy bỏ")
            .build()

        // Gọi màn hình Quét Vân Tay của Android lên
        biometricPrompt.authenticate(promptInfo)
    }

    private fun openMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Tiêu hủy màn hình Khóa để tránh dùng phím Back quay lại đây
    }
}
