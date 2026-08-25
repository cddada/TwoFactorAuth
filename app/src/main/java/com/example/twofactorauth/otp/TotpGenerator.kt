package com.example.twofactorauth.otp

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TotpGenerator @Inject constructor() {

    fun generate(
        secret: ByteArray,
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30,
        timeMillis: Long = System.currentTimeMillis()
    ): String {
        require(period > 0) { "TOTP period must be positive" }
        val counter = timeMillis / 1000 / period
        return generateWithCounter(secret, counter, algorithm, digits)
    }

    fun generateWithCounter(
        secret: ByteArray,
        counter: Long,
        algorithm: String = "SHA1",
        digits: Int = 6
    ): String {
        val data = OtpUtils.longToBytes(counter)
        val hmacAlgorithm = OtpUtils.getHmacAlgorithm(algorithm)
        val hash = OtpUtils.hmacHash(hmacAlgorithm, secret, data)
        val code = OtpUtils.dynamicTruncate(hash, digits)
        return OtpUtils.formatOtp(code, digits)
    }

    fun getRemainingSeconds(period: Int = 30): Int {
        require(period > 0) { "TOTP period must be positive" }
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        return (period - (currentTimeSeconds % period)).toInt()
    }

    fun getProgress(period: Int = 30): Float {
        require(period > 0) { "TOTP period must be positive" }
        val remaining = getRemainingSeconds(period)
        return remaining.toFloat() / period
    }
}
