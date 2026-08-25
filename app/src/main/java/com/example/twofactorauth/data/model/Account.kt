package com.example.twofactorauth.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Display info
    val issuer: String,
    val accountName: String,

    // Type
    val type: AccountType,

    // TOTP/HOTP parameters (from otpauth:// URI)
    @ColumnInfo(name = "encrypted_secret")
    val secret: String,  // Encrypted via AES-256-GCM

    val algorithm: String = "SHA1",  // SHA1 / SHA256 / SHA512
    val digits: Int = 6,             // 6 or 8
    val period: Int = 30,            // TOTP period in seconds

    // HOTP
    val counter: Long = 0,

    // UI
    val iconUri: String? = null,
    val sortOrder: Int = 0,
    val createdTime: Long = System.currentTimeMillis(),
    val lastUsedTime: Long? = null
)
