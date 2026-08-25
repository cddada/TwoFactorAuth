package com.example.twofactorauth

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.twofactorauth.navigation.NavGraph
import com.example.twofactorauth.security.AppLockManager
import com.example.twofactorauth.security.BiometricAuthenticator
import com.example.twofactorauth.ui.lock.LockScreen
import com.example.twofactorauth.ui.theme.TwoFactorAuthTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var appLockManager: AppLockManager
    @Inject lateinit var biometricAuthenticator: BiometricAuthenticator

    private var isUnlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLogger.log("MainActivity", "onCreate started")

        // Check if lock is needed on start
        try {
            isUnlocked = !appLockManager.needsAuthentication()
            CrashLogger.log("MainActivity", "needsAuthentication result: isUnlocked=$isUnlocked")
        } catch (e: Exception) {
            CrashLogger.log("MainActivity", "needsAuthentication error: ${e.message}")
            isUnlocked = true
        }

        setContent {
            TwoFactorAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isUnlocked || !appLockManager.isLockEnabled) {
                        CrashLogger.log("MainActivity", "Showing NavGraph")
                        NavGraph()
                    } else {
                        CrashLogger.log("MainActivity", "Showing LockScreen")
                        LockScreen(
                            onUnlockClick = {
                                CrashLogger.log("MainActivity", "onUnlockClick triggered")
                                try {
                                    biometricAuthenticator.authenticate(
                                        activity = this@MainActivity,
                                        onSuccess = {
                                            CrashLogger.log("MainActivity", "Biometric auth success")
                                            appLockManager.onUnlocked()
                                            isUnlocked = true
                                        },
                                        onError = { code, msg ->
                                            CrashLogger.log("MainActivity", "Biometric auth error: $code - $msg")
                                        },
                                        onFailed = {
                                            CrashLogger.log("MainActivity", "Biometric auth failed")
                                        }
                                    )
                                } catch (e: Exception) {
                                    CrashLogger.log("MainActivity", "Biometric auth exception: ${e.message}")
                                    isUnlocked = true
                                }
                            }
                        )
                    }
                }
            }
        }
        CrashLogger.log("MainActivity", "onCreate completed")
    }

    override fun onStart() {
        super.onStart()
        isUnlocked = !appLockManager.needsAuthentication()
    }

    override fun onStop() {
        appLockManager.onBackground()
        super.onStop()
    }
}
