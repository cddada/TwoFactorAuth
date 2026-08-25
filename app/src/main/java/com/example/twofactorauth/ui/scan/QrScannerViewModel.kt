package com.example.twofactorauth.ui.scan

import androidx.lifecycle.ViewModel
import com.example.twofactorauth.otp.OtpUriParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class QrScannerUiState(
    val isProcessing: Boolean = false,
    val error: String? = null,
    val scanResult: String? = null
)

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val otpUriParser: OtpUriParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrScannerUiState())
    val uiState: StateFlow<QrScannerUiState> = _uiState.asStateFlow()

    fun processBarcode(rawValue: String): Boolean {
        if (_uiState.value.isProcessing) return false

        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        return try {
            if (!rawValue.startsWith("otpauth://", ignoreCase = true)) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "不是有效的两步验证二维码（需要 otpauth:// 开头）"
                )
                return false
            }

            // Validate by parsing
            otpUriParser.parse(rawValue)

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                scanResult = rawValue
            )
            true
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = "二维码内容无效: ${e.message}"
            )
            false
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = QrScannerUiState()
    }
}
