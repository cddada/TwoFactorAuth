package com.example.twofactorauth.data.model

enum class AccountType {
    TOTP,
    HOTP,
    SMS,
    EMAIL;

    companion object {
        fun fromString(value: String): AccountType {
            return when (value.uppercase()) {
                "TOTP" -> TOTP
                "HOTP" -> HOTP
                "SMS" -> SMS
                "EMAIL" -> EMAIL
                else -> TOTP
            }
        }
    }
}
