package com.example.twofactorauth.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secretCache: SecretCache
) {

    companion object {
        private const val PREFS_NAME = "app_lock_prefs"
        private const val KEY_LOCK_ENABLED = "lock_enabled"
        private const val KEY_LAST_BACKGROUND_TIME = "last_background_time"
        private const val AUTO_LOCK_TIMEOUT_MS = 5 * 60 * 1000L  // 5 minutes
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    var isLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_LOCK_ENABLED, value).apply()

    private var _isUnlocked = false
    val isUnlocked: Boolean get() = _isUnlocked

    fun needsAuthentication(): Boolean {
        if (!isLockEnabled) return false
        if (!_isUnlocked) return true

        val backgroundTime = try {
            prefs.getLong(KEY_LAST_BACKGROUND_TIME, 0)
        } catch (e: Exception) {
            0L
        }
        val elapsed = System.currentTimeMillis() - backgroundTime
        if (backgroundTime > 0 && elapsed >= AUTO_LOCK_TIMEOUT_MS) {
            onLocked()
            return true
        }

        try {
            prefs.edit().remove(KEY_LAST_BACKGROUND_TIME).apply()
        } catch (e: Exception) {
            // Ignore
        }
        return false
    }

    fun onUnlocked() {
        _isUnlocked = true
        prefs.edit().remove(KEY_LAST_BACKGROUND_TIME).apply()
    }

    fun onLocked() {
        _isUnlocked = false
        secretCache.invalidateAll()
    }

    fun onBackground() {
        if (isLockEnabled && _isUnlocked) {
            prefs.edit()
                .putLong(KEY_LAST_BACKGROUND_TIME, System.currentTimeMillis())
                .apply()
        }
    }

    fun toggleLock(enabled: Boolean) {
        isLockEnabled = enabled
        if (!enabled) {
            _isUnlocked = true
            prefs.edit().remove(KEY_LAST_BACKGROUND_TIME).apply()
        } else {
            onLocked()
        }
    }
}
