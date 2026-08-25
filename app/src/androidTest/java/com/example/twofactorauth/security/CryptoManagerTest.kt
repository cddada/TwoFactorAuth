package com.example.twofactorauth.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    @Test
    fun encryptAndDecryptRoundTrip() {
        val cryptoManager = CryptoManager()
        val plainText = "JBSWY3DPEHPK3PXP"

        val decrypted = cryptoManager.decrypt(cryptoManager.encrypt(plainText))

        assertEquals(plainText, decrypted)
    }

    @Test
    fun samePlaintextUsesDifferentIv() {
        val cryptoManager = CryptoManager()
        val first = cryptoManager.encrypt("secret")
        val second = cryptoManager.encrypt("secret")

        assertNotEquals(first, second)
    }
}
