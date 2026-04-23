package com.app.qlct

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.lifecycle.lifecycleScope
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarSettings)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        val prefs = AppPrefs.get(this)

        // 1. PIN Lock Toggle
        val switchPinLock = findViewById<SwitchMaterial>(R.id.switchPinLock)
        switchPinLock.isChecked = prefs.getBoolean(AppPrefs.KEY_PIN_LOCKED, false)
        switchPinLock.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    val input = android.widget.EditText(this@SettingsActivity)
                    input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    input.hint = "Nhập 4 số bí mật"
                    input.filters = arrayOf(android.text.InputFilter.LengthFilter(4))
                    val container = android.widget.LinearLayout(this@SettingsActivity)
                    container.setPadding(50, 20, 50, 0)
                    container.addView(input)

                    androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                        .setTitle("Thiết lập mã PIN")
                        .setView(container)
                        .setCancelable(false)
                        .setPositiveButton("Lưu chốt") { _, _ ->
                            val pin = input.text.toString()
                            if (pin.length >= 4) {
                                prefs.edit()
                                    .putBoolean(AppPrefs.KEY_PIN_LOCKED, true)
                                    .putString(AppPrefs.KEY_APP_PIN, pin)
                                    .apply()
                                Toast.makeText(this@SettingsActivity, "Đã kích hoạt khóa PIN thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingsActivity, "Mã PIN phải đủ 4 số!", Toast.LENGTH_SHORT).show()
                                switchPinLock.isChecked = false
                            }
                        }
                        .setNegativeButton("Hủy") { _, _ ->
                            switchPinLock.isChecked = false
                        }
                        .show()
                } else {
                    prefs.edit()
                        .putBoolean(AppPrefs.KEY_PIN_LOCKED, false)
                        .putString(AppPrefs.KEY_APP_PIN, "")
                        .apply()
                    Toast.makeText(this@SettingsActivity, "Đã tháo bỏ khóa bảo mật", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. Biometric
        val switchBiometric = findViewById<SwitchMaterial>(R.id.switchBiometric)
        switchBiometric.isChecked = prefs.getBoolean(AppPrefs.KEY_BIO_LOCKED, false)
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(AppPrefs.KEY_BIO_LOCKED, isChecked).apply()
            val msg = if (isChecked) "Bật nhận diện sinh trắc học thành công" else "Đã tắt vân tay"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 3. Dark Mode
        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        switchDarkMode.isChecked = prefs.getBoolean(AppPrefs.KEY_DARK_MODE, false)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(AppPrefs.KEY_DARK_MODE, isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            // recreate() để Activity hiện tại render lại ngay với theme mới
            recreate()
        }

        // 4. Backup (giữ nguyên stub — sẽ implement sau)
        findViewById<LinearLayout>(R.id.btnBackup).setOnClickListener {
            Toast.makeText(this, "Tính năng đang được phát triển...", Toast.LENGTH_SHORT).show()
        }

        // 5. DANGER ZONE: Reset Database & Clear Data
        findViewById<LinearLayout>(R.id.btnResetData).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("CẢNH BÁO NGUY HIỂM TỘT ĐỘ 🚨")
                .setMessage("Hành động này sẽ Xóa Vĩnh Viễn 100% tất cả thẻ ngân hàng, tài khoản, lịch sử giao dịch và mọi thiết lập của bạn khỏi cơ sở dữ liệu. Không thể khôi phục!\n\nBạn có chắc chắn muốn làm sạch toàn bộ để bắt đầu lại không?")
                .setPositiveButton("Xóa toàn bộ (Reset)") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@SettingsActivity).clearAllTables()
                        // Xóa tất cả preferences (trừ Dark Mode)
                        val isDark = prefs.getBoolean(AppPrefs.KEY_DARK_MODE, false)
                        prefs.edit().clear().putBoolean(AppPrefs.KEY_DARK_MODE, isDark).apply()

                        launch(Dispatchers.Main) {
                            Toast.makeText(this@SettingsActivity, "Thành công: Ứng dụng đã được dọn sạch rác và trở về lại trạng thái Khởi Thủy!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Hủy bỏ lệnh", null)
                .show()
        }
    }
}
