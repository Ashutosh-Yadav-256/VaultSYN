package com.vaultsync.domain.model

sealed class AppError(override val message: String, override val cause: Throwable? = null) : Exception(message, cause) {
    data class StorageError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class CryptoError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class SyncError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class IntegrityError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class NotFoundError(override val message: String) : AppError(message)
    data class DuplicateError(override val message: String) : AppError(message)
}
