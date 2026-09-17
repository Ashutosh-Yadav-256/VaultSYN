package com.vaultsync.domain.usecase

import com.vaultsync.domain.repository.CryptoService

class CalculateFileHashUseCase(
    private val cryptoService: CryptoService
) {
    suspend operator fun invoke(data: ByteArray): String {
        return cryptoService.calculateSha256(data)
    }
}
