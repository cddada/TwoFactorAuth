package com.example.twofactorauth.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.twofactorauth.CrashLogger
import com.example.twofactorauth.data.database.AppDatabase
import com.example.twofactorauth.data.database.AccountDao
import com.example.twofactorauth.data.database.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        CrashLogger.log("DatabaseModule", "Creating database")
        return try {
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "twofactorauth.db"
            )
                .addMigrations(*Migrations.getAllMigrations())
                .build()
                .also {
                    CrashLogger.log("DatabaseModule", "Database created successfully")
                }
        } catch (e: Exception) {
            CrashLogger.log("DatabaseModule", "Database creation failed: ${e.message}")
            throw e
        }
    }

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }
}
