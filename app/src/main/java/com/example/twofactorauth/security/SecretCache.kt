package com.example.twofactorauth.security

import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class SecretCache @Inject constructor(
    private val cryptoManager: CryptoManager
) {
    // accountId -> decrypted secret
    private val cache = ConcurrentHashMap<Long, String>()

    fun getDecryptedSecret(accountId: Long, encryptedSecret: String): String {
        return cache.computeIfAbsent(accountId) {
            cryptoManager.decrypt(encryptedSecret)
        }
    }

    fun invalidate(accountId: Long) {
        cache.remove(accountId)
    }

    fun invalidateAll() {
        cache.clear()
    }

    fun preload(accounts: Map<Long, String>) {
        accounts.forEach { (id, encryptedSecret) ->
            if (!cache.containsKey(id)) {
                try {
                    cache[id] = cryptoManager.decrypt(encryptedSecret)
                } catch (e: Exception) {
                    // Skip invalid entries
                }
            }
        }
    }
}
