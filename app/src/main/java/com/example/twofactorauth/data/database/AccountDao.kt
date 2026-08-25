package com.example.twofactorauth.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.twofactorauth.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, createdTime DESC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE issuer LIKE '%' || :query || '%' OR accountName LIKE '%' || :query || '%' ORDER BY sortOrder ASC")
    fun searchAccounts(query: String): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?

    @Query("SELECT * FROM accounts WHERE type = :type ORDER BY sortOrder ASC")
    fun getAccountsByType(type: String): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE accounts SET counter = :counter WHERE id = :id")
    suspend fun updateCounter(id: Long, counter: Long)

    @Query("UPDATE accounts SET counter = counter + 1, lastUsedTime = :timestamp WHERE id = :id AND type = 'HOTP'")
    suspend fun consumeHotp(id: Long, timestamp: Long): Int

    @Query("UPDATE accounts SET lastUsedTime = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM accounts")
    fun getCount(): Flow<Int>
}
