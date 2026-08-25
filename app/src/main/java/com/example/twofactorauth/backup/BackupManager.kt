package com.example.twofactorauth.backup

import android.content.Context
import android.net.Uri
import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.repository.AccountRepository
import com.example.twofactorauth.otp.OtpUtils
import com.example.twofactorauth.security.CryptoManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountRepository: AccountRepository,
    private val cryptoManager: CryptoManager,
    private val backupEncryptor: BackupEncryptor
) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToUri(uri: Uri, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(password.isNotBlank()) { "Backup password must not be blank" }
            val accounts = accountRepository.getAllAccounts().first()

            val backupAccounts = accounts.map { account ->
                // Decrypt secret from DB encryption, then re-encrypt with backup password
                val decryptedSecret = cryptoManager.decrypt(account.secret)

                BackupAccount(
                    issuer = account.issuer,
                    accountName = account.accountName,
                    type = account.type,
                    secret = decryptedSecret,
                    algorithm = account.algorithm,
                    digits = account.digits,
                    period = account.period,
                    counter = account.counter,
                    iconUri = account.iconUri
                )
            }

            val backupData = BackupData(
                version = 1,
                accounts = backupAccounts
            )

            val json = gson.toJson(backupData)
            val encrypted = backupEncryptor.encrypt(json, password)

            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: throw IllegalArgumentException("Cannot open backup destination")
            outputStream.use { output ->
                output.write(encrypted.toByteArray(Charsets.UTF_8))
            }
        }
    }

    suspend fun importFromUri(uri: Uri, password: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            require(password.isNotBlank()) { "Backup password must not be blank" }
            val encrypted = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readText()
            } ?: throw IllegalArgumentException("Cannot read file")

            val json = backupEncryptor.decrypt(encrypted, password)
            val backupData = gson.fromJson(json, BackupData::class.java)
                ?: throw IllegalArgumentException("Invalid backup data")
            require(backupData.version == 1) {
                "Unsupported backup version: ${backupData.version}"
            }

            val accounts = backupData.accounts.map { backupAccount ->
                validateBackupAccount(backupAccount)
                val normalizedSecret = OtpUtils.normalizeBase32(backupAccount.secret)
                val encryptedSecret = cryptoManager.encrypt(normalizedSecret)

                Account(
                    issuer = backupAccount.issuer,
                    accountName = backupAccount.accountName,
                    type = backupAccount.type,
                    secret = encryptedSecret,
                    algorithm = backupAccount.algorithm,
                    digits = backupAccount.digits,
                    period = backupAccount.period,
                    counter = backupAccount.counter,
                    iconUri = backupAccount.iconUri
                )
            }

            accountRepository.insertAll(accounts)
            accounts.size
        }
    }

    private fun validateBackupAccount(account: BackupAccount) {
        val secret = OtpUtils.normalizeBase32(account.secret)
        require(account.issuer.isNotBlank()) { "Backup account issuer must not be blank" }
        require(account.accountName.isNotBlank()) { "Backup account name must not be blank" }
        require(account.type == com.example.twofactorauth.data.model.AccountType.TOTP ||
                account.type == com.example.twofactorauth.data.model.AccountType.HOTP) {
            "Unsupported backup account type: ${account.type}"
        }
        require(OtpUtils.isValidBase32(secret)) {
            "Backup account contains an invalid Base32 secret"
        }
        require(account.algorithm.uppercase() in OtpUtils.VALID_ALGORITHMS) {
            "Backup account contains an unsupported algorithm"
        }
        require(account.digits == 6 || account.digits == 8) {
            "Backup account digits must be 6 or 8"
        }
        if (account.type == com.example.twofactorauth.data.model.AccountType.TOTP) {
            require(account.period in 1..300) { "Backup TOTP period is invalid" }
        } else {
            require(account.counter >= 0) { "Backup HOTP counter is invalid" }
        }
    }
}
