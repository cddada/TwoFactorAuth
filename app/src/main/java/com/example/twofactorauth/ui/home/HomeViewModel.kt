package com.example.twofactorauth.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twofactorauth.CrashLogger
import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.model.AccountType
import com.example.twofactorauth.data.repository.AccountRepository
import com.example.twofactorauth.domain.DeleteAccountUseCase
import com.example.twofactorauth.domain.GenerateOtpUseCase
import com.example.twofactorauth.otp.OtpTicker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtpDisplayItem(
    val account: Account,
    val currentCode: String,
    val remainingSeconds: Int,
    val progress: Float
)

data class HomeUiState(
    val accounts: List<OtpDisplayItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val generateOtpUseCase: GenerateOtpUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val otpTicker: OtpTicker
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        CrashLogger.log("HomeViewModel", "HomeViewModel initialized")

        viewModelScope.launch {
            try {
                combine(
                    accountRepository.getAllAccounts(),
                    otpTicker.tickFlow(),
                    _searchQuery
                ) { accounts, tick, query ->
                    val filtered = if (query.isBlank()) accounts
                    else accounts.filter {
                        it.issuer.contains(query, ignoreCase = true) ||
                                it.accountName.contains(query, ignoreCase = true)
                    }

                    val displayItems = filtered.map { account ->
                        val result = generateOtpUseCase.execute(account, tick.currentTimeMillis)
                        OtpDisplayItem(
                            account = account,
                            currentCode = result.code,
                            remainingSeconds = result.remainingSeconds,
                            progress = result.progress
                        )
                    }

                    HomeUiState(
                        accounts = displayItems,
                        searchQuery = query,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                CrashLogger.log("HomeViewModel", "Error in init: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            deleteAccountUseCase.execute(account)
            generateOtpUseCase.invalidateCache(account.id)
        }
    }

    fun incrementHotpCounter(account: Account) {
        viewModelScope.launch {
            accountRepository.consumeHotp(account.id)
        }
    }
}
