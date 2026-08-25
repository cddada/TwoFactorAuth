package com.example.twofactorauth.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.twofactorauth.data.model.Account

@Database(
    entities = [Account::class],
    version = DatabaseVersion.CURRENT,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}
