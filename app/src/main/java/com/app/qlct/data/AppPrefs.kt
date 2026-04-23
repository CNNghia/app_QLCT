package com.app.qlct.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Wrapper tập trung quản lý SharedPreferences được mã hóa (AES-256).
 * Mọi dữ liệu nhạy cảm (PIN, cài đặt bảo mật, ngân sách) đều lưu qua lớp này.
 */
object AppPrefs {

    private const val PREFS_NAME = "AppConfig_Encrypted"

    // ── Keys ────────────────────────────────────────────────────────────
    const val KEY_PIN_LOCKED = "PIN_LOCKED"
    const val KEY_APP_PIN = "APP_PIN"
    const val KEY_BIO_LOCKED = "BIO_LOCKED"
    const val KEY_BUDGET_LIMIT = "BUDGET_LIMIT"
    const val KEY_DARK_MODE = "DARK_MODE"

    // ── Internal ─────────────────────────────────────────────────────────
    @Volatile
    private var instance: SharedPreferences? = null

    fun get(context: Context): SharedPreferences {
        return instance ?: synchronized(this) {
            instance ?: createPrefs(context).also { instance = it }
        }
    }

    private fun createPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback về plaintext nếu thiết bị không hỗ trợ (rất hiếm)
            context.applicationContext.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE)
        }
    }
}
