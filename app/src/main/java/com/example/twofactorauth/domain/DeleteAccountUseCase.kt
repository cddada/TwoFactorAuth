package com.example.twofactorauth.domain

import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.repository.AccountRepository
import com.example.twofactorauth.security.SecretCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val secretCache: SecretCache
) {
    suspend fun execute(account: Account) {
        secretCache.invalidate(account.id)
        accountRepository.deleteById(account.id)
    }

    suspend fun executeById(accountId: Long) {
        secretCache.invalidate(accountId)
        accountRepository.deleteById(accountId)
    }
}
