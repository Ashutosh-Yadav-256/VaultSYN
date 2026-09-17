package com.vaultsync.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vaultsync.domain.model.Conflict
import com.vaultsync.domain.model.ConflictResolution

@Entity(tableName = "conflicts")
data class ConflictEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "document_id")
    val documentId: String,
    @ColumnInfo(name = "document_name")
    val documentName: String,
    @ColumnInfo(name = "local_hash")
    val localHash: String,
    @ColumnInfo(name = "remote_hash")
    val remoteHash: String,
    @ColumnInfo(name = "local_modified_at")
    val localModifiedAt: Long,
    @ColumnInfo(name = "remote_modified_at")
    val remoteModifiedAt: Long,
    @ColumnInfo(name = "detected_at")
    val detectedAt: Long,
    val resolution: String?
) {
    fun toDomain(): Conflict {
        return Conflict(
            id = id,
            documentId = documentId,
            documentName = documentName,
            localHash = localHash,
            remoteHash = remoteHash,
            localModifiedAt = localModifiedAt,
            remoteModifiedAt = remoteModifiedAt,
            detectedAt = detectedAt,
            resolution = resolution?.let {
                try {
                    ConflictResolution.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
        )
    }

    companion object {
        fun fromDomain(c: Conflict): ConflictEntity {
            return ConflictEntity(
                id = c.id,
                documentId = c.documentId,
                documentName = c.documentName,
                localHash = c.localHash,
                remoteHash = c.remoteHash,
                localModifiedAt = c.localModifiedAt,
                remoteModifiedAt = c.remoteModifiedAt,
                detectedAt = c.detectedAt,
                resolution = c.resolution?.name
            )
        }
    }
}
