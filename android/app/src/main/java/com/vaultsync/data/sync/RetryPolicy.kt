package com.vaultsync.data.sync

import kotlin.math.min

class RetryPolicy(
    val baseDelayMs: Long = 1000L,
    val maxDelayMs: Long = 8000L,
    val maxRetries: Int = 3
) {
    fun shouldRetry(attempt: Int): Boolean {
        return attempt < maxRetries
    }

    fun calculateDelayMs(attempt: Int): Long {
        if (attempt <= 0) return baseDelayMs
        val factor = 1L shl attempt
        return min(baseDelayMs * factor, maxDelayMs)
    }
}
