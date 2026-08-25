package com.example.twofactorauth.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twofactorauth.data.model.AccountType
import com.example.twofactorauth.domain.AddAccountUseCase
import com.example.twofactorauth.otp.OtpUriParser
import com.example.twofactorauth.otp.OtpUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAccountUiState(
    val issuer: String = "",
    val accountName: String = "",
    val secret: String = "",
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val type: AccountType = AccountType.TOTP,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val addAccountUseCase: AddAccountUseCase,
    private val otpUriParser: OtpUriParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    fun loadFromUri(uri: String): Boolean {
        return try {
            val params = otpUriParser.parse(uri)
            _uiState.value = AddAccountUiState(
                issuer = params.issuer,
                accountName = params.accountName,
                secret = params.secret,
                algorithm = params.algorithm,
                digits = params.digits,
                period = params.period,
                type = params.type
            )
            true
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "解析二维码失败: ${e.message}"
            )
            false
        }
    }

    fun updateIssuer(value: String) {
        _uiState.value = _uiState.value.copy(issuer = value, error = null)
    }

    fun updateAccountName(value: String) {
        _uiState.value = _uiState.value.copy(accountName = value, error = null)
    }

    fun updateSecret(value: String) {
        _uiState.value = _uiState.value.copy(secret = OtpUtils.normalizeBase32(value), error = null)
    }

    fun updateAlgorithm(value: String) {
        _uiState.value = _uiState.value.copy(algorithm = value)
    }

    fun updateDigits(value: Int) {
        _uiState.value = _uiState.value.copy(digits = value)
    }

    fun updatePeriod(value: Int) {
        _uiState.value = _uiState.value.copy(period = value)
    }

    fun updateType(value: AccountType) {
        _uiState.value = _uiState.value.copy(type = value)
    }

    fun save() {
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true, error = null)

        viewModelScope.launch {
            when (val result = addAccountUseCase.execute(
                issuer = state.issuer,
                accountName = state.accountName,
                secret = state.secret,
                type = state.type,
                algorithm = state.algorithm,
                digits = state.digits,
                period = state.period
            )) {
                is AddAccountUseCase.Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                }
                is AddAccountUseCase.Result.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                }
            }
        }
    }
}
