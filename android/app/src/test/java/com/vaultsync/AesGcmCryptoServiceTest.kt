package com.vaultsync

import com.vaultsync.data.security.AesGcmCryptoService
import com.vaultsync.data.security.InMemoryKeyProvider
import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.model.EncryptedPayload
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class AesGcmCryptoServiceTest {

    private val cryptoService = AesGcmCryptoService(InMemoryKeyProvider())

    @Test
    fun `encrypt and decrypt successfully performs roundtrip on data`() {
        runBlocking {
            val originalPlaintext = "Top Secret Financial Report for VaultSync".toByteArray(Charsets.UTF_8)
            val payload = cryptoService.encrypt(originalPlaintext)

            assertFalse("Ciphertext must not equal plaintext", payload.ciphertext.contentEquals(originalPlaintext))

            val decrypted = cryptoService.decrypt(payload)
            assertArrayEquals("Decrypted bytes must match original plaintext exactly", originalPlaintext, decrypted)
        }
    }

    @Test
    fun `decrypt throws CryptoError if ciphertext is tampered`() {
        runBlocking {
            val originalPlaintext = "Integrity check test payload".toByteArray()
            val payload = cryptoService.encrypt(originalPlaintext)

            val tamperedCiphertext = payload.ciphertext.clone()
            tamperedCiphertext[tamperedCiphertext.size - 1] = (tamperedCiphertext[tamperedCiphertext.size - 1].toInt() xor 0xFF).toByte()

            val tamperedPayload = EncryptedPayload(ciphertext = tamperedCiphertext, iv = payload.iv)

            assertThrows(AppError.CryptoError::class.java) {
                runBlocking {
                    cryptoService.decrypt(tamperedPayload)
                }
            }
        }
    }
}
