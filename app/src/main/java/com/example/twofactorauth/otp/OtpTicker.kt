package com.example.twofactorauth.otp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpTicker @Inject constructor() {

    data class TickData(
        val currentTimeMillis: Long
    )

    fun tickFlow(): Flow<TickData> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(TickData(currentTimeMillis = now))

            // Calculate precise delay to next second boundary
            val nextSecondMillis = ((now / 1000 + 1) * 1000) - now
            delay(nextSecondMillis.coerceIn(1, 1000))
        }
    }
}
