package com.example.twofactorauth.data.repository

import com.example.twofactorauth.data.database.AccountDao
import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.model.AccountType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {

    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()

    fun searchAccounts(query: String): Flow<List<Account>> =
        accountDao.searchAccounts(query)

    suspend fun getAccountById(id: Long): Account? = accountDao.getAccountById(id)

    fun getAccountsByType(type: AccountType): Flow<List<Account>> =
        accountDao.getAccountsByType(type.name)

    suspend fun insert(account: Account): Long = accountDao.insert(account)

    suspend fun insertAll(accounts: List<Account>) = accountDao.insertAll(accounts)

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(account: Account) = accountDao.delete(account)

    suspend fun deleteById(id: Long) = accountDao.deleteById(id)

    suspend fun updateCounter(id: Long, counter: Long) =
        accountDao.updateCounter(id, counter)

    suspend fun consumeHotp(id: Long): Boolean =
        accountDao.consumeHotp(id, System.currentTimeMillis()) == 1

    suspend fun updateLastUsed(id: Long) =
        accountDao.updateLastUsed(id, System.currentTimeMillis())

    fun getCount(): Flow<Int> = accountDao.getCount()
}
