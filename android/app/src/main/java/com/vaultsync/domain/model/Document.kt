package com.vaultsync.domain.model

enum class SyncStatus {
    SYNCED,
    PENDING,
    SYNCING,
    CONFLICT,
    FAILED
}

data class FileMetadata(
    val size: Long,
    val mimeType: String,
    val sha256: String,
    val createdAt: Long,
    val modifiedAt: Long
)

data class Document(
    val id: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val localPath: String,
    val sha256: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
