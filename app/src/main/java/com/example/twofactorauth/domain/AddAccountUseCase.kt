package com.example.twofactorauth.domain

import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.model.AccountType
import com.example.twofactorauth.data.repository.AccountRepository
import com.example.twofactorauth.otp.OtpUtils
import com.example.twofactorauth.security.CryptoManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val cryptoManager: CryptoManager
) {
    sealed class Result {
        data class Success(val account: Account) : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun execute(
        issuer: String,
        accountName: String,
        secret: String,
        type: AccountType = AccountType.TOTP,
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30
    ): Result {
        // Validation
        if (issuer.isBlank()) {
            return Result.Error("请输入发行方")
        }
        if (secret.isBlank()) {
            return Result.Error("请输入密钥")
        }
        if (!isValidBase32(secret)) {
            return Result.Error("密钥格式无效（需要Base32编码，至少16个字符）")
        }
        if (type != AccountType.TOTP && type != AccountType.HOTP) {
            return Result.Error("当前仅支持 TOTP 和 HOTP")
        }
        if (algorithm.uppercase() !in OtpUtils.VALID_ALGORITHMS) {
            return Result.Error("不支持的算法")
        }
        if (digits !in setOf(6, 8)) {
            return Result.Error("验证码位数只能是 6 或 8")
        }
        if (period !in 1..300) {
            return Result.Error("验证码周期必须在 1 到 300 秒之间")
        }

        return try {
            val encryptedSecret = cryptoManager.encrypt(OtpUtils.normalizeBase32(secret))

            val account = Account(
                issuer = issuer.trim(),
                accountName = accountName.trim().ifEmpty { issuer.trim() },
                type = type,
                secret = encryptedSecret,
                algorithm = algorithm.uppercase(),
                digits = digits,
                period = period
            )

            val id = accountRepository.insert(account)
            Result.Success(account.copy(id = id))
        } catch (e: Exception) {
            Result.Error("保存失败: ${e.message}")
        }
    }

    private fun isValidBase32(secret: String): Boolean {
        val clean = OtpUtils.normalizeBase32(secret)
        return OtpUtils.isValidBase32(clean) && clean.length >= 16
    }
}
