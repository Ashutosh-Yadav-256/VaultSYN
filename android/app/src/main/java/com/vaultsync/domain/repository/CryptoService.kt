package com.vaultsync.domain.repository

import com.vaultsync.domain.model.EncryptedPayload

interface CryptoService {
    suspend fun encrypt(data: ByteArray, keyAlias: String = DEFAULT_KEY_ALIAS): EncryptedPayload
    suspend fun decrypt(payload: EncryptedPayload, keyAlias: String = DEFAULT_KEY_ALIAS): ByteArray
    suspend fun calculateSha256(data: ByteArray): String

    companion object {
        const val DEFAULT_KEY_ALIAS = "vaultsync_master_key"
    }
}
