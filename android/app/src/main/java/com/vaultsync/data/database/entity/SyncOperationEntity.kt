package com.vaultsync.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vaultsync.domain.model.OperationStatus
import com.vaultsync.domain.model.OperationType
import com.vaultsync.domain.model.SyncOperation

@Entity(
    tableName = "sync_operations",
    indices = [
        Index(value = ["document_id"]),
        Index(value = ["status"])
    ]
)
data class SyncOperationEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "document_id")
    val documentId: String,
    @ColumnInfo(name = "operation_type")
    val operationType: String,
    val status: String,
    @ColumnInfo(name = "retry_count")
    val retryCount: Int,
    @ColumnInfo(name = "started_at")
    val startedAt: Long?,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
    @ColumnInfo(name = "error_message")
    val errorMessage: String?
) {
    fun toDomain(): SyncOperation {
        return SyncOperation(
            id = id,
            documentId = documentId,
            operationType = try {
                OperationType.valueOf(operationType)
            } catch (e: Exception) {
                OperationType.UPLOAD
            },
            status = try {
                OperationStatus.valueOf(status)
            } catch (e: Exception) {
                OperationStatus.PENDING
            },
            retryCount = retryCount,
            startedAt = startedAt,
            completedAt = completedAt,
            errorMessage = errorMessage
        )
    }

    companion object {
        fun fromDomain(op: SyncOperation): SyncOperationEntity {
            return SyncOperationEntity(
                id = op.id,
                documentId = op.documentId,
                operationType = op.operationType.name,
                status = op.status.name,
                retryCount = op.retryCount,
                startedAt = op.startedAt,
                completedAt = op.completedAt,
                errorMessage = op.errorMessage
            )
        }
    }
}
