package com.example.twofactorauth.otp

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TotpGeneratorTest {

    private lateinit var totpGenerator: TotpGenerator

    // RFC 6238 test secret: "12345678901234567890"
    private val rfcSecret = "12345678901234567890".toByteArray(Charsets.US_ASCII)

    @Before
    fun setup() {
        totpGenerator = TotpGenerator()
    }

    @Test
    fun `RFC 6238 SHA1 test vectors`() {
        // RFC 6238 Appendix B - SHA1 test vectors
        val testCases = listOf(
            59L to "94287082",
            1111111109L to "07081804",
            1111111111L to "14050471",
            1234567890L to "89005924",
            2000000000L to "69279037",
            20000000000L to "65353130"
        )

        testCases.forEach { (timeSeconds, expectedCode) ->
            val timeMillis = timeSeconds * 1000
            val code = totpGenerator.generate(
                secret = rfcSecret,
                algorithm = "SHA1",
                digits = 8,
                period = 30,
                timeMillis = timeMillis
            )
            assertEquals(
                "SHA1 failed for time=$timeSeconds",
                expectedCode,
                code
            )
        }
    }

    @Test
    fun `RFC 6238 SHA256 test vectors`() {
        // RFC 6238 SHA256 test vectors
        val sha256Secret = "12345678901234567890123456789012".toByteArray(Charsets.US_ASCII)

        val testCases = listOf(
            59L to "46119246",
            1111111109L to "68084774",
            1111111111L to "67062674",
            1234567890L to "91819424",
            2000000000L to "90698825",
            20000000000L to "77737706"
        )

        testCases.forEach { (timeSeconds, expectedCode) ->
            val timeMillis = timeSeconds * 1000
            val code = totpGenerator.generate(
                secret = sha256Secret,
                algorithm = "SHA256",
                digits = 8,
                period = 30,
                timeMillis = timeMillis
            )
            assertEquals(
                "SHA256 failed for time=$timeSeconds",
                expectedCode,
                code
            )
        }
    }

    @Test
    fun `RFC 6238 SHA512 test vectors`() {
        // RFC 6238 SHA512 test vectors
        val sha512Secret = "1234567890123456789012345678901234567890123456789012345678901234".toByteArray(Charsets.US_ASCII)

        val testCases = listOf(
            59L to "90693936",
            1111111109L to "25091201",
            1111111111L to "99943326",
            1234567890L to "93441116",
            2000000000L to "38618901",
            20000000000L to "47863826"
        )

        testCases.forEach { (timeSeconds, expectedCode) ->
            val timeMillis = timeSeconds * 1000
            val code = totpGenerator.generate(
                secret = sha512Secret,
                algorithm = "SHA512",
                digits = 8,
                period = 30,
                timeMillis = timeMillis
            )
            assertEquals(
                "SHA512 failed for time=$timeSeconds",
                expectedCode,
                code
            )
        }
    }

    @Test
    fun `6-digit TOTP generation`() {
        val timeMillis = 59000L
        val code = totpGenerator.generate(
            secret = rfcSecret,
            algorithm = "SHA1",
            digits = 6,
            period = 30,
            timeMillis = timeMillis
        )
        // Last 6 digits of 94287082 = 287082
        assertEquals("287082", code)
    }

    @Test
    fun `remaining seconds calculation`() {
        // At time 59s (within first 30s period), remaining should be 1
        // At time 30s, remaining should be 30
        val remaining30 = totpGenerator.getRemainingSeconds(30)
        assert(remaining30 in 1..30)
    }

    @Test
    fun `progress calculation`() {
        val progress = totpGenerator.getProgress(30)
        assert(progress in 0f..1f)
    }
}
