package com.vaultsync.data.security

import com.vaultsync.domain.model.AppError
import com.vaultsync.domain.model.EncryptedPayload
import com.vaultsync.domain.repository.CryptoService
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AesGcmCryptoService(
    private val keyProvider: KeyProvider = AndroidKeystoreHelper()
) : CryptoService {

    override suspend fun encrypt(
        data: ByteArray,
        keyAlias: String
    ): EncryptedPayload = withContext(Dispatchers.Default) {
        try {
            val secretKey = keyProvider.getOrCreateKey(keyAlias)
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv ?: throw AppError.CryptoError("Failed to generate cipher IV")
            val ciphertext = cipher.doFinal(data)
            EncryptedPayload(ciphertext = ciphertext, iv = iv)
        } catch (e: Exception) {
            throw AppError.CryptoError("Encryption failed: ${e.message}", e)
        }
    }

    override suspend fun decrypt(
        payload: EncryptedPayload,
        keyAlias: String
    ): ByteArray = withContext(Dispatchers.Default) {
        try {
            val secretKey = keyProvider.getOrCreateKey(keyAlias)
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, payload.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            cipher.doFinal(payload.ciphertext)
        } catch (e: Exception) {
            throw AppError.CryptoError("Decryption failed (tampering or key mismatch): ${e.message}", e)
        }
    }

    override suspend fun calculateSha256(data: ByteArray): String = withContext(Dispatchers.Default) {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
