package com.vaultsync.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vaultsync.VaultSyncApplication
import com.vaultsync.ui.screens.VaultSyncApp
import com.vaultsync.ui.theme.VaultSyncTheme
import com.vaultsync.ui.viewmodel.ConflictsViewModel
import com.vaultsync.ui.viewmodel.DashboardViewModel
import com.vaultsync.ui.viewmodel.DocumentDetailViewModel
import com.vaultsync.ui.viewmodel.DocumentListViewModel
import com.vaultsync.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as VaultSyncApplication

        val dashboardViewModel = DashboardViewModel(
            getSyncStatusUseCase = app.getSyncStatusUseCase,
            startSyncUseCase = app.startSyncUseCase
        )

        val documentListViewModel = DocumentListViewModel(
            getDocumentsUseCase = app.getDocumentsUseCase,
            importDocumentUseCase = app.importDocumentUseCase,
            deleteDocumentUseCase = app.deleteDocumentUseCase
        )

        val documentDetailViewModel = DocumentDetailViewModel(
            documentRepository = app.documentRepository,
            verifyIntegrityUseCase = app.verifyIntegrityUseCase
        )

        val conflictsViewModel = ConflictsViewModel(
            syncRepository = app.syncRepository,
            resolveConflictUseCase = app.resolveConflictUseCase
        )

        val settingsViewModel = SettingsViewModel(
            documentRepository = app.documentRepository
        )

        setContent {
            VaultSyncTheme {
                VaultSyncApp(
                    dashboardViewModel = dashboardViewModel,
                    documentListViewModel = documentListViewModel,
                    documentDetailViewModel = documentDetailViewModel,
                    conflictsViewModel = conflictsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
