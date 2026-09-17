package com.vaultsync.domain.model

enum class OperationType {
    UPLOAD,
    DOWNLOAD,
    DELETE
}

enum class OperationStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

data class SyncOperation(
    val id: String,
    val documentId: String,
    val operationType: OperationType,
    val status: OperationStatus = OperationStatus.PENDING,
    val retryCount: Int = 0,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val errorMessage: String? = null
)
