package com.example.twofactorauth.otp

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotpGenerator @Inject constructor() {

    fun generate(
        secret: ByteArray,
        counter: Long,
        algorithm: String = "SHA1",
        digits: Int = 6
    ): String {
        require(counter >= 0) { "HOTP counter must not be negative" }
        val data = OtpUtils.longToBytes(counter)
        val hmacAlgorithm = OtpUtils.getHmacAlgorithm(algorithm)
        val hash = OtpUtils.hmacHash(hmacAlgorithm, secret, data)
        val code = OtpUtils.dynamicTruncate(hash, digits)
        return OtpUtils.formatOtp(code, digits)
    }
}
