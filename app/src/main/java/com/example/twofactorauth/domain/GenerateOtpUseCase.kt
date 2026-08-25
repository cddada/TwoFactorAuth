package com.example.twofactorauth.domain

import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.model.AccountType
import com.example.twofactorauth.otp.HotpGenerator
import com.example.twofactorauth.otp.OtpUtils
import com.example.twofactorauth.otp.TotpGenerator
import com.example.twofactorauth.security.SecretCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerateOtpUseCase @Inject constructor(
    private val totpGenerator: TotpGenerator,
    private val hotpGenerator: HotpGenerator,
    private val secretCache: SecretCache
) {
    data class OtpResult(
        val code: String,
        val remainingSeconds: Int,
        val progress: Float
    )

    // Cache: accountId -> (lastCounter, lastCode). Avoids recomputing HMAC every second.
    private val totpCache = mutableMapOf<Long, Pair<Long, String>>()

    fun execute(account: Account, timeMillis: Long = System.currentTimeMillis()): OtpResult {
        val remaining = if (account.type == AccountType.TOTP && account.period > 0) {
            account.period - ((timeMillis / 1000) % account.period).toInt()
        } else {
            0
        }
        val progress = if (account.type == AccountType.TOTP && account.period > 0) {
            remaining.toFloat() / account.period
        } else {
            0f
        }

        val code = when (account.type) {
            AccountType.TOTP -> {
                val counter = timeMillis / 1000 / account.period
                val cached = totpCache[account.id]
                if (cached != null && cached.first == counter) {
                    cached.second
                } else {
                    val computed = computeTotpCode(account, timeMillis)
                    totpCache[account.id] = counter to computed
                    computed
                }
            }
            AccountType.HOTP -> computeHotpCode(account)
            else -> "------"
        }

        return OtpResult(code = code, remainingSeconds = remaining, progress = progress)
    }

    /** Remove cached entry when an account is deleted so the map doesn't grow unbounded. */
    fun invalidateCache(accountId: Long) {
        totpCache.remove(accountId)
    }

    private fun computeTotpCode(account: Account, timeMillis: Long): String = try {
        val secretBytes = OtpUtils.base32Decode(
            secretCache.getDecryptedSecret(account.id, account.secret)
        )
        totpGenerator.generate(
            secret = secretBytes,
            algorithm = account.algorithm,
            digits = account.digits,
            period = account.period,
            timeMillis = timeMillis
        )
    } catch (_: Exception) { "ERROR" }

    private fun computeHotpCode(account: Account): String = try {
        val secretBytes = OtpUtils.base32Decode(
            secretCache.getDecryptedSecret(account.id, account.secret)
        )
        hotpGenerator.generate(
            secret = secretBytes,
            counter = account.counter,
            algorithm = account.algorithm,
            digits = account.digits
        )
    } catch (_: Exception) { "ERROR" }
}
