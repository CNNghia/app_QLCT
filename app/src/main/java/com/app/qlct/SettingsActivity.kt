package com.app.qlct

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.lifecycle.lifecycleScope
import com.app.qlct.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarSettings)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences("AppConfig", MODE_PRIVATE)

        // 1. PIN Lock Toggles
        val switchPinLock = findViewById<SwitchMaterial>(R.id.switchPinLock)
        switchPinLock.isChecked = prefs.getBoolean("PIN_LOCKED", false)
        switchPinLock.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) { // Chỉ kích hoạt khi người dùng NGUYÊN BẢN CỐ Ý bấm
                if (isChecked) {
                    // Show dialog to input PIN
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
                                prefs.edit().putBoolean("PIN_LOCKED", true).putString("APP_PIN", pin).apply()
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
                    prefs.edit().putBoolean("PIN_LOCKED", false).putString("APP_PIN", "").apply()
                    Toast.makeText(this@SettingsActivity, "Đã tháo bỏ khóa bảo mật", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. Biometric
        val switchBiometric = findViewById<SwitchMaterial>(R.id.switchBiometric)
        switchBiometric.isChecked = prefs.getBoolean("BIO_LOCKED", false)
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("BIO_LOCKED", isChecked).apply()
            val msg = if (isChecked) "Bật nhận diện sinh trắc học thành công" else "Đã tắt vân tay"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 3. UI Buttons (Theme, Currency, Backup)
        findViewById<LinearLayout>(R.id.btnTheme).setOnClickListener {
            Toast.makeText(this, "Hệ thống Dark Mode sẽ sớm ra mắt ở phiên bản Update V2", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.btnCurrency).setOnClickListener {
            Toast.makeText(this, "Hệ quy chiếu Tiền tệ hiện tại đã chốt chặt ở mức VNĐ", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.btnBackup).setOnClickListener {
            Toast.makeText(this, "Đang trích xuất toàn bộ dữ liệu ra Excel...", Toast.LENGTH_SHORT).show()
        }

        // 4. DANGER ZONE: Reset Database & Clear Data
        findViewById<LinearLayout>(R.id.btnResetData).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("CẢNH BÁO NGUY HIỂM TỘT ĐỘ \uD83D\uDEA8")
                .setMessage("Hành động này sẽ Xóa Vĩnh Viễn 100% tất cả thẻ ngân hàng, tài khoản, lịch sử giao dịch và mọi thiết lập của bạn khỏi cơ sở dữ liệu. Không thể khôi phục!\n\nBạn có chắc chắn muốn làm sạch toàn bộ để bắt đầu lại không?")
                .setPositiveButton("Xóa toàn bộ (Reset)") { _, _ ->
                    // Phải gọi xóa Database ở luồng chạy ngầm để không bị Crash app do block luồng chính
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@SettingsActivity).clearAllTables()
                        // Xóa sạch bộ nhớ tạm Preference (Ngân sách)
                        prefs.edit().clear().apply()
                        
                        // Gọi kết quả về lại luồng chính UI để in thông báo
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
