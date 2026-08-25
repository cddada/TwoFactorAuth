package com.example.twofactorauth.otp

import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.model.AccountType
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpUriParser @Inject constructor() {

    data class OtpParams(
        val type: AccountType,
        val issuer: String,
        val accountName: String,
        val secret: String,
        val algorithm: String = "SHA1",
        val digits: Int = 6,
        val period: Int = 30,
        val counter: Long = 0
    )

    fun parse(uri: String): OtpParams {
        if (!uri.startsWith("otpauth://", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid OTP URI: must start with otpauth://")
        }

        val parsed = URI(uri)
        val type = when (parsed.host?.lowercase()) {
            "totp" -> AccountType.TOTP
            "hotp" -> AccountType.HOTP
            else -> throw IllegalArgumentException("Unknown OTP type: ${parsed.host}")
        }

        // Parse label (issuer:accountName or just accountName)
        val rawLabel = decodePathComponent(parsed.rawPath.orEmpty().trimStart('/'))
        val (issuerFromLabel, accountName) = parseLabel(rawLabel)
        require(accountName.isNotBlank()) { "OTP account name must not be blank" }

        // Parse query parameters
        val params = parseQueryParams(parsed.rawQuery ?: "")

        val secret = params["secret"]?.let { OtpUtils.normalizeBase32(it) }
            ?: throw IllegalArgumentException("Missing required parameter: secret")
        val issuer = params["issuer"] ?: issuerFromLabel ?: ""
        val algorithm = (params["algorithm"] ?: "SHA1").uppercase()
        val digits = params["digits"]?.toIntOrNull() ?: 6
        val period = params["period"]?.toIntOrNull() ?: 30
        val counter = if (type == AccountType.HOTP) {
            params["counter"]?.toLongOrNull()
                ?: throw IllegalArgumentException("HOTP URI requires a counter")
        } else {
            0
        }

        validateParameters(type, secret, algorithm, digits, period, counter)

        return OtpParams(
            type = type,
            issuer = issuer,
            accountName = accountName,
            secret = secret,
            algorithm = algorithm,
            digits = digits,
            period = period,
            counter = counter
        )
    }

    fun toUri(account: Account, plainTextSecret: String): String {
        require(account.type == AccountType.TOTP || account.type == AccountType.HOTP) {
            "Only TOTP and HOTP accounts can be exported"
        }
        val secret = OtpUtils.normalizeBase32(plainTextSecret)
        validateParameters(
            account.type,
            secret,
            account.algorithm.uppercase(),
            account.digits,
            account.period,
            account.counter
        )

        val label = if (account.issuer.isNotEmpty()) {
            "${account.issuer}:${account.accountName}"
        } else {
            account.accountName
        }

        val params = mutableListOf(
            "secret" to secret,
            "algorithm" to account.algorithm,
            "digits" to account.digits.toString()
        )
        if (account.issuer.isNotEmpty()) params += "issuer" to account.issuer
        if (account.type == AccountType.TOTP) params += "period" to account.period.toString()

        if (account.type == AccountType.HOTP) {
            params += "counter" to account.counter.toString()
        }

        val queryString = params.joinToString("&") { (key, value) ->
            "${encodeComponent(key)}=${encodeComponent(value)}"
        }
        return "otpauth://${account.type.name.lowercase()}/${encodeComponent(label)}?$queryString"
    }

    private fun parseLabel(label: String): Pair<String?, String> {
        val colonIndex = label.indexOf(':')
        return if (colonIndex >= 0) {
            Pair(
                label.substring(0, colonIndex).trim(),
                label.substring(colonIndex + 1).trim()
            )
        } else {
            Pair(null, label.trim())
        }
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()

        return query.split("&")
            .mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    Pair(
                        URLDecoder.decode(parts[0], "UTF-8").lowercase(),
                        URLDecoder.decode(parts[1], "UTF-8")
                    )
                } else null
            }
            .toMap()
    }

    private fun validateParameters(
        type: AccountType,
        secret: String,
        algorithm: String,
        digits: Int,
        period: Int,
        counter: Long
    ) {
        require(OtpUtils.isValidBase32(secret)) { "Secret must be valid Base32" }
        require(algorithm in OtpUtils.VALID_ALGORITHMS) { "Unsupported OTP algorithm: $algorithm" }
        require(digits == 6 || digits == 8) { "OTP digits must be 6 or 8" }
        if (type == AccountType.TOTP) {
            require(period in 1..300) { "TOTP period must be between 1 and 300 seconds" }
        } else {
            require(counter >= 0) { "HOTP counter must not be negative" }
        }
    }

    private fun encodeComponent(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private fun decodePathComponent(value: String): String =
        URLDecoder.decode(value.replace("+", "%2B"), "UTF-8")
}
