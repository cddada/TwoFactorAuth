package com.example.twofactorauth.otp

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class OtpUtilsTest {

    @Test
    fun `base32 decode standard string`() {
        // "Hello" in Base32 is "JBSWY3DP"
        val result = OtpUtils.base32Decode("JBSWY3DP")
        assertArrayEquals("Hello".toByteArray(), result)
    }

    @Test
    fun `base32 decode with padding`() {
        val result = OtpUtils.base32Decode("JBSWY3DP===")
        assertArrayEquals("Hello".toByteArray(), result)
    }

    @Test
    fun `base32 decode with spaces`() {
        val result = OtpUtils.base32Decode("JBSW Y3DP")
        assertArrayEquals("Hello".toByteArray(), result)
    }

    @Test
    fun `base32 decode lowercase`() {
        val result = OtpUtils.base32Decode("jbswy3dp")
        assertArrayEquals("Hello".toByteArray(), result)
    }

    @Test
    fun `long to bytes conversion`() {
        val result = OtpUtils.longToBytes(1L)
        assertEquals(8, result.size)
        assertEquals(0.toByte(), result[6])
        assertEquals(1.toByte(), result[7])
    }

    @Test
    fun `format OTP with leading zeros`() {
        assertEquals("000123", OtpUtils.formatOtp(123, 6))
        assertEquals("001234", OtpUtils.formatOtp(1234, 6))
        assertEquals("123456", OtpUtils.formatOtp(123456, 6))
    }

    @Test
    fun `HMAC algorithm mapping`() {
        assertEquals("HmacSHA1", OtpUtils.getHmacAlgorithm("SHA1"))
        assertEquals("HmacSHA256", OtpUtils.getHmacAlgorithm("SHA256"))
        assertEquals("HmacSHA512", OtpUtils.getHmacAlgorithm("SHA512"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown HMAC algorithm is rejected`() {
        OtpUtils.getHmacAlgorithm("unknown")
    }
}
