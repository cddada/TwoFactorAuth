package com.example.twofactorauth.otp

import com.example.twofactorauth.data.model.Account
import com.example.twofactorauth.data.model.AccountType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OtpUriParserTest {

    private lateinit var parser: OtpUriParser

    @Before
    fun setup() {
        parser = OtpUriParser()
    }

    @Test
    fun `parse standard TOTP URI`() {
        val uri = "otpauth://totp/GitHub:user@gmail.com?secret=JBSWY3DPEHPK3PXP&issuer=GitHub&algorithm=SHA1&digits=6&period=30"

        val result = parser.parse(uri)

        assertEquals(AccountType.TOTP, result.type)
        assertEquals("GitHub", result.issuer)
        assertEquals("user@gmail.com", result.accountName)
        assertEquals("JBSWY3DPEHPK3PXP", result.secret)
        assertEquals("SHA1", result.algorithm)
        assertEquals(6, result.digits)
        assertEquals(30, result.period)
    }

    @Test
    fun `parse HOTP URI with counter`() {
        val uri = "otpauth://hotp/Example:alice@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Example&counter=42"

        val result = parser.parse(uri)

        assertEquals(AccountType.HOTP, result.type)
        assertEquals("Example", result.issuer)
        assertEquals("alice@example.com", result.accountName)
        assertEquals("JBSWY3DPEHPK3PXP", result.secret)
        assertEquals(42L, result.counter)
    }

    @Test
    fun `parse URI with SHA256 algorithm`() {
        val uri = "otpauth://totp/Test:user@test.com?secret=JBSWY3DPEHPK3PXP&algorithm=SHA256&digits=8"

        val result = parser.parse(uri)

        assertEquals("SHA256", result.algorithm)
        assertEquals(8, result.digits)
    }

    @Test
    fun `parse URI with default values`() {
        val uri = "otpauth://totp/Simple:user@simple.com?secret=JBSWY3DPEHPK3PXP"

        val result = parser.parse(uri)

        assertEquals("SHA1", result.algorithm)
        assertEquals(6, result.digits)
        assertEquals(30, result.period)
    }

    @Test
    fun `parse URI with encoded characters`() {
        val uri = "otpauth://totp/My%20Service:user%40email.com?secret=JBSWY3DPEHPK3PXP&issuer=My%20Service"

        val result = parser.parse(uri)

        assertEquals("My Service", result.issuer)
        assertEquals("user@email.com", result.accountName)
    }

    @Test
    fun `parse URI with issuer in label only`() {
        val uri = "otpauth://totp/LabelOnly?secret=JBSWY3DPEHPK3PXP"

        val result = parser.parse(uri)

        assertEquals("", result.issuer)
        assertEquals("LabelOnly", result.accountName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse invalid URI throws exception`() {
        parser.parse("not-a-valid-uri")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse URI without secret throws exception`() {
        parser.parse("otpauth://totp/Test:user@test.com")
    }

    @Test
    fun `convert account to URI and parse back`() {
        val account = Account(
            id = 1,
            issuer = "GitHub",
            accountName = "user@gmail.com",
            type = AccountType.TOTP,
            secret = "JBSWY3DPEHPK3PXP",
            algorithm = "SHA1",
            digits = 6,
            period = 30
        )

        val uri = parser.toUri(account, account.secret)
        val parsed = parser.parse(uri)

        assertEquals(account.issuer, parsed.issuer)
        assertEquals(account.accountName, parsed.accountName)
        assertEquals(account.secret, parsed.secret)
        assertEquals(account.algorithm, parsed.algorithm)
        assertEquals(account.digits, parsed.digits)
        assertEquals(account.period, parsed.period)
    }

    @Test
    fun `URI export encodes label and query values`() {
        val account = Account(
            issuer = "A & B",
            accountName = "user+tag@example.com",
            type = AccountType.TOTP,
            secret = "JBSWY3DPEHPK3PXP"
        )

        val parsed = parser.parse(parser.toUri(account, account.secret))

        assertEquals(account.issuer, parsed.issuer)
        assertEquals(account.accountName, parsed.accountName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero TOTP period is rejected`() {
        parser.parse("otpauth://totp/Test:user?secret=JBSWY3DPEHPK3PXP&period=0")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `HOTP URI without counter is rejected`() {
        parser.parse("otpauth://hotp/Test:user?secret=JBSWY3DPEHPK3PXP")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `encrypted database secret cannot be exported as OTP URI`() {
        parser.toUri(
            Account(
                issuer = "Test",
                accountName = "user",
                type = AccountType.TOTP,
                secret = "database ciphertext is not used"
            ),
            "iv:ciphertext"
        )
    }
}
