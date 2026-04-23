package com.app.qlct

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.app.qlct.data.AppPrefs
import java.util.concurrent.Executor

class PinLockActivity : AppCompatActivity() {

    // Bảo vệ brute-force: khóa sau N lần nhập sai
    private var failedAttempts = 0
    private val maxAttempts = 5
    private val lockoutDurationMs = 30_000L // 30 giây
    private var lockoutTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Áp dụng Dark Mode trước khi setContentView
        val prefs = AppPrefs.get(this)
        val isDarkMode = prefs.getBoolean(AppPrefs.KEY_DARK_MODE, false)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )

        val isLocked = prefs.getBoolean(AppPrefs.KEY_PIN_LOCKED, false)
        val isBioLocked = prefs.getBoolean(AppPrefs.KEY_BIO_LOCKED, false)
        val savedPin = prefs.getString(AppPrefs.KEY_APP_PIN, "") ?: ""

        // Không khóa gì cả → Vào thẳng app
        if (!isLocked && !isBioLocked) {
            openMainApp()
            return
        }

        setContentView(R.layout.activity_pin_lock)

        val btnRepromptBio = findViewById<android.widget.TextView>(R.id.btnRepromptBio)

        // Gọi Sinh trắc học với delay 500ms tránh lỗi Cold-boot Cancel
        if (isBioLocked) {
            btnRepromptBio.visibility = android.view.View.VISIBLE
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                triggerBiometricLogin(isLocked)
            }, 500)

            btnRepromptBio.setOnClickListener {
                triggerBiometricLogin(isLocked)
            }
        }

        val etPin = findViewById<EditText>(R.id.etPin)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)

        if (!isLocked) {
            etPin.visibility = android.view.View.GONE
            btnUnlock.visibility = android.view.View.GONE
        }

        btnUnlock.setOnClickListener {
            // Chặn nếu đang trong thời gian lockout
            if (failedAttempts >= maxAttempts) {
                Toast.makeText(this, "Tài khoản đang bị khóa. Vui lòng chờ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val inputPin = etPin.text.toString()
            if (inputPin == savedPin) {
                failedAttempts = 0 // Reset khi đúng
                openMainApp()
            } else {
                failedAttempts++
                val remaining = maxAttempts - failedAttempts
                if (failedAttempts >= maxAttempts) {
                    startLockoutTimer(btnUnlock)
                    Toast.makeText(this, "Đã nhập sai $maxAttempts lần! Vui lòng đợi 30 giây.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Sai mã PIN! Còn $remaining lần thử.", Toast.LENGTH_SHORT).show()
                }
                etPin.text.clear()
            }
        }
    }

    /** Bắt đầu đếm ngược lockout, disable nút Unlock */
    private fun startLockoutTimer(btnUnlock: Button) {
        btnUnlock.isEnabled = false
        lockoutTimer?.cancel()
        lockoutTimer = object : CountDownTimer(lockoutDurationMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secsLeft = millisUntilFinished / 1000
                btnUnlock.text = "Đợi ${secsLeft}s..."
            }
            override fun onFinish() {
                failedAttempts = 0
                btnUnlock.isEnabled = true
                btnUnlock.text = "ĐĂNG NHẬP"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        lockoutTimer?.cancel()
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
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(applicationContext, "Hủy xác minh. $errString", Toast.LENGTH_SHORT).show()
                    }
                    if (!hasPinFallback) {
                        finish()
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Bảo mật Sinh Trắc Học")
            .setSubtitle("Vui lòng chạm Cảm biến vân tay hoặc Nhìn vào Camera")
            .setNegativeButtonText(if (hasPinFallback) "Sử dụng Mã PIN dự phòng" else "Hủy bỏ")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun openMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
