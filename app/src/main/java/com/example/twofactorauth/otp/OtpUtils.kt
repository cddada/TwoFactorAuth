package com.example.twofactorauth.otp

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object OtpUtils {

    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /** Precompiled regex for Base32 validation – avoids recompilation on every call. */
    val BASE32_REGEX: Regex = Regex("^[A-Z2-7]+$")

    /** Supported OTP HMAC algorithms. */
    val VALID_ALGORITHMS: Set<String> = setOf("SHA1", "SHA256", "SHA512")

    /** Normalise a user-supplied Base32 secret: uppercase, strip spaces and padding. */
    fun normalizeBase32(input: String): String =
        input.uppercase().replace(" ", "").trimEnd('=')

    /** Validate that a normalised string is legal Base32. */
    fun isValidBase32(normalized: String): Boolean =
        normalized.isNotEmpty() && BASE32_REGEX.matches(normalized)

    fun base32Decode(input: String): ByteArray {
        val cleanInput = normalizeBase32(input)
        var buffer = 0
        var bitsLeft = 0
        val result = mutableListOf<Byte>()

        for (c in cleanInput) {
            val value = BASE32_CHARS.indexOf(c)
            if (value < 0) throw IllegalArgumentException("Invalid Base32 character: $c")

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                bitsLeft -= 8
                result.add((buffer shr bitsLeft and 0xFF).toByte())
            }
        }

        return result.toByteArray()
    }

    fun hmacHash(algorithm: String, key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key, algorithm))
        return mac.doFinal(data)
    }

    fun dynamicTruncate(hash: ByteArray, digits: Int): Int {
        require(digits == 6 || digits == 8) { "OTP digits must be 6 or 8" }
        require(hash.size >= 20) { "HMAC result is too short" }
        val offset = hash.last().toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        val modulus = if (digits == 6) 1_000_000 else 100_000_000
        return binary % modulus
    }

    fun longToBytes(value: Long): ByteArray {
        return ByteBuffer.allocate(8).putLong(value).array()
    }

    fun formatOtp(code: Int, digits: Int): String {
        return code.toString().padStart(digits, '0')
    }

    fun getHmacAlgorithm(algorithm: String): String {
        return when (algorithm.uppercase()) {
            "SHA1" -> "HmacSHA1"
            "SHA256" -> "HmacSHA256"
            "SHA512" -> "HmacSHA512"
            else -> throw IllegalArgumentException("Unsupported OTP algorithm: $algorithm")
        }
    }
}
