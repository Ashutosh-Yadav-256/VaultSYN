package com.vaultsync.data.sync

import com.vaultsync.domain.model.Conflict
import com.vaultsync.domain.model.Document
import java.util.UUID

class ConflictDetector {
    fun detectConflict(
        localDoc: Document,
        remoteHash: String,
        remoteModifiedAt: Long
    ): Conflict? {
        if (!localDoc.sha256.equals(remoteHash, ignoreCase = true)) {
            return Conflict(
                id = UUID.randomUUID().toString(),
                documentId = localDoc.id,
                documentName = localDoc.name,
                localHash = localDoc.sha256,
                remoteHash = remoteHash,
                localModifiedAt = localDoc.modifiedAt,
                remoteModifiedAt = remoteModifiedAt
            )
        }
        return null
    }
}
