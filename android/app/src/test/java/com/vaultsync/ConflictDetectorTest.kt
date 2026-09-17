package com.vaultsync

import com.vaultsync.data.sync.ConflictDetector
import com.vaultsync.domain.model.Document
import com.vaultsync.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConflictDetectorTest {

    private val detector = ConflictDetector()

    private val baseDoc = Document(
        id = "doc-1",
        name = "report.pdf",
        size = 1024L,
        mimeType = "application/pdf",
        localPath = "vault_doc-1.enc",
        sha256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        createdAt = 1000L,
        modifiedAt = 2000L,
        syncStatus = SyncStatus.SYNCED
    )

    @Test
    fun `detectConflict returns null when hashes match identically`() {
        val conflict = detector.detectConflict(
            localDoc = baseDoc,
            remoteHash = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            remoteModifiedAt = 2500L
        )
        assertNull("Identical hashes must not trigger a conflict", conflict)
    }

    @Test
    fun `detectConflict returns conflict model when hashes diverge`() {
        val remoteHash = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        val conflict = detector.detectConflict(
            localDoc = baseDoc,
            remoteHash = remoteHash,
            remoteModifiedAt = 3000L
        )

        assertNotNull(conflict)
        assertEquals("doc-1", conflict!!.documentId)
        assertEquals("report.pdf", conflict.documentName)
        assertEquals(baseDoc.sha256, conflict.localHash)
        assertEquals(remoteHash, conflict.remoteHash)
        assertEquals(2000L, conflict.localModifiedAt)
        assertEquals(3000L, conflict.remoteModifiedAt)
    }
}
