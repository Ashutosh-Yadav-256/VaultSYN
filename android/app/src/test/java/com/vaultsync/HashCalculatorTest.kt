package com.vaultsync

import com.vaultsync.data.security.AesGcmCryptoService
import com.vaultsync.data.security.InMemoryKeyProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class HashCalculatorTest {

    private val cryptoService = AesGcmCryptoService(InMemoryKeyProvider())

    @Test
    fun `calculateSha256 produces exact NIST digest for empty array`() = runBlocking {
        val emptyBytes = ByteArray(0)
        val expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val actual = cryptoService.calculateSha256(emptyBytes)
        assertEquals(expected, actual.lowercase())
    }

    @Test
    fun `calculateSha256 produces exact digest for hello world string`() = runBlocking {
        val bytes = "hello world".toByteArray(Charsets.UTF_8)
        val expected = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
        val actual = cryptoService.calculateSha256(bytes)
        assertEquals(expected, actual.lowercase())
    }

    @Test
    fun `calculateSha256 is deterministic across multiple evaluations`() = runBlocking {
        val payload = "vaultsync_deterministic_data_block".toByteArray()
        val hash1 = cryptoService.calculateSha256(payload)
        val hash2 = cryptoService.calculateSha256(payload)
        assertEquals(hash1, hash2)
    }
}
