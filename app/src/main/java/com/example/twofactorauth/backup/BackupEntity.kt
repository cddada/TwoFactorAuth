package com.example.twofactorauth.backup

import com.example.twofactorauth.data.model.AccountType

data class BackupData(
    val version: Int = 1,
    val exportTime: Long = System.currentTimeMillis(),
    val accounts: List<BackupAccount>
)

data class BackupAccount(
    val issuer: String,
    val accountName: String,
    val type: AccountType,
    val secret: String,  // Will be re-encrypted with backup password
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Long = 0,
    val iconUri: String? = null
)
