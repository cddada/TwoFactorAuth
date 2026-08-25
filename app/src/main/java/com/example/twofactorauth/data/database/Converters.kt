package com.example.twofactorauth.data.database

import androidx.room.TypeConverter
import com.example.twofactorauth.data.model.AccountType

class Converters {

    @TypeConverter
    fun fromAccountType(type: AccountType): String {
        return type.name
    }

    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return AccountType.fromString(value)
    }
}
