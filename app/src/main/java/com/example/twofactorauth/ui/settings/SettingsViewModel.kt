package com.example.twofactorauth.ui.settings

import androidx.lifecycle.ViewModel
import com.example.twofactorauth.security.AppLockManager
import com.example.twofactorauth.security.BiometricAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val lockEnabled: Boolean = false,
    val authenticationAvailable: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appLockManager: AppLockManager,
    biometricAuthenticator: BiometricAuthenticator
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            lockEnabled = appLockManager.isLockEnabled,
            authenticationAvailable = biometricAuthenticator.canAuthenticate()
        )
    )
    val uiState = _uiState.asStateFlow()

    fun setLockEnabled(enabled: Boolean) {
        if (enabled && !_uiState.value.authenticationAvailable) {
            _uiState.value = _uiState.value.copy(
                message = "请先在系统设置中配置屏幕锁或生物识别"
            )
            return
        }

        appLockManager.toggleLock(enabled)
        _uiState.value = _uiState.value.copy(lockEnabled = enabled, message = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
