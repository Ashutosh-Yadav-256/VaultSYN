package com.vaultsync

import android.app.Application
import com.vaultsync.data.database.VaultDatabase
import com.vaultsync.data.filesystem.LocalFileStorage
import com.vaultsync.data.repository.DocumentRepositoryImpl
import com.vaultsync.data.repository.SyncRepositoryImpl
import com.vaultsync.data.security.AesGcmCryptoService
import com.vaultsync.data.security.AndroidKeystoreHelper
import com.vaultsync.data.sync.CrashRecoveryHandler
import com.vaultsync.data.sync.SyncEngine
import com.vaultsync.domain.repository.CryptoService
import com.vaultsync.domain.repository.DocumentRepository
import com.vaultsync.domain.repository.FileStorage
import com.vaultsync.domain.repository.SyncController
import com.vaultsync.domain.repository.SyncRepository
import com.vaultsync.domain.usecase.CalculateFileHashUseCase
import com.vaultsync.domain.usecase.DeleteDocumentUseCase
import com.vaultsync.domain.usecase.DetectDuplicatesUseCase
import com.vaultsync.domain.usecase.GetDocumentsUseCase
import com.vaultsync.domain.usecase.GetSyncStatusUseCase
import com.vaultsync.domain.usecase.ImportDocumentUseCase
import com.vaultsync.domain.usecase.PauseSyncUseCase
import com.vaultsync.domain.usecase.ResolveConflictUseCase
import com.vaultsync.domain.usecase.ResumeSyncUseCase
import com.vaultsync.domain.usecase.StartSyncUseCase
import com.vaultsync.domain.usecase.VerifyIntegrityUseCase
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VaultSyncApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: VaultDatabase
        private set

    lateinit var fileStorage: FileStorage
        private set

    lateinit var cryptoService: CryptoService
        private set

    lateinit var documentRepository: DocumentRepository
        private set

    lateinit var syncRepository: SyncRepository
        private set

    lateinit var syncController: SyncController
        private set

    // Use cases
    lateinit var getDocumentsUseCase: GetDocumentsUseCase
        private set
    lateinit var importDocumentUseCase: ImportDocumentUseCase
        private set
    lateinit var deleteDocumentUseCase: DeleteDocumentUseCase
        private set
    lateinit var calculateFileHashUseCase: CalculateFileHashUseCase
        private set
    lateinit var verifyIntegrityUseCase: VerifyIntegrityUseCase
        private set
    lateinit var detectDuplicatesUseCase: DetectDuplicatesUseCase
        private set
    lateinit var startSyncUseCase: StartSyncUseCase
        private set
    lateinit var pauseSyncUseCase: PauseSyncUseCase
        private set
    lateinit var resumeSyncUseCase: ResumeSyncUseCase
        private set
    lateinit var resolveConflictUseCase: ResolveConflictUseCase
        private set
    lateinit var getSyncStatusUseCase: GetSyncStatusUseCase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = VaultDatabase.getInstance(this)
        fileStorage = LocalFileStorage(File(filesDir, "vault"))
        cryptoService = AesGcmCryptoService(AndroidKeystoreHelper())

        documentRepository = DocumentRepositoryImpl(database.documentDao())
        syncRepository = SyncRepositoryImpl(database.syncOperationDao(), database.conflictDao())
        syncController = SyncEngine(
            documentRepository = documentRepository,
            syncRepository = syncRepository,
            fileStorage = fileStorage,
            cryptoService = cryptoService
        )

        getDocumentsUseCase = GetDocumentsUseCase(documentRepository)
        importDocumentUseCase = ImportDocumentUseCase(documentRepository, syncRepository, fileStorage, cryptoService)
        deleteDocumentUseCase = DeleteDocumentUseCase(documentRepository, syncRepository, fileStorage)
        calculateFileHashUseCase = CalculateFileHashUseCase(cryptoService)
        verifyIntegrityUseCase = VerifyIntegrityUseCase(documentRepository, fileStorage, cryptoService)
        detectDuplicatesUseCase = DetectDuplicatesUseCase(documentRepository)
        startSyncUseCase = StartSyncUseCase(syncController)
        pauseSyncUseCase = PauseSyncUseCase(syncController)
        resumeSyncUseCase = ResumeSyncUseCase(syncController)
        resolveConflictUseCase = ResolveConflictUseCase(syncRepository, documentRepository, fileStorage)
        getSyncStatusUseCase = GetSyncStatusUseCase(documentRepository, syncRepository, syncController)

        // Crash recovery on cold start
        applicationScope.launch {
            val recoveryHandler = CrashRecoveryHandler(syncRepository, documentRepository, fileStorage, cryptoService)
            recoveryHandler.recover()
        }
    }

    companion object {
        lateinit var instance: VaultSyncApplication
            private set
    }
}
