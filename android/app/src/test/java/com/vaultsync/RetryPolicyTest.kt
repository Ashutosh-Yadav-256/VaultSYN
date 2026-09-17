package com.vaultsync

import com.vaultsync.data.sync.RetryPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPolicyTest {

    private val policy = RetryPolicy(
        baseDelayMs = 1000L,
        maxDelayMs = 8000L,
        maxRetries = 3
    )

    @Test
    fun `shouldRetry allows up to maxRetries attempts`() {
        assertTrue(policy.shouldRetry(0))
        assertTrue(policy.shouldRetry(1))
        assertTrue(policy.shouldRetry(2))
        assertFalse(policy.shouldRetry(3))
        assertFalse(policy.shouldRetry(4))
    }

    @Test
    fun `calculateDelayMs follows exponential progression`() {
        assertEquals(1000L, policy.calculateDelayMs(0))
        assertEquals(2000L, policy.calculateDelayMs(1))
        assertEquals(4000L, policy.calculateDelayMs(2))
        assertEquals(8000L, policy.calculateDelayMs(3))
    }

    @Test
    fun `calculateDelayMs clamps to maxDelayMs on high attempt counts`() {
        assertEquals(8000L, policy.calculateDelayMs(4))
        assertEquals(8000L, policy.calculateDelayMs(10))
    }
}
