package com.vaultsync.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.SyncStatus

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["sha256"]),
        Index(value = ["sync_status"])
    ]
)
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val size: Long,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    @ColumnInfo(name = "local_path")
    val localPath: String,
    val sha256: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,
    @ColumnInfo(name = "sync_status")
    val syncStatus: String
) {
    fun toDomain(): Document {
        return Document(
            id = id,
            name = name,
            size = size,
            mimeType = mimeType,
            localPath = localPath,
            sha256 = sha256,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            syncStatus = try {
                SyncStatus.valueOf(syncStatus)
            } catch (e: Exception) {
                SyncStatus.PENDING
            }
        )
    }

    companion object {
        fun fromDomain(doc: Document): DocumentEntity {
            return DocumentEntity(
                id = doc.id,
                name = doc.name,
                size = doc.size,
                mimeType = doc.mimeType,
                localPath = doc.localPath,
                sha256 = doc.sha256,
                createdAt = doc.createdAt,
                modifiedAt = doc.modifiedAt,
                syncStatus = doc.syncStatus.name
            )
        }
    }
}
