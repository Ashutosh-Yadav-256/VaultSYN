package com.vaultsync.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vaultsync.ui.theme.AccentCyan
import com.vaultsync.ui.theme.CardBackground
import com.vaultsync.ui.theme.Slate400
import com.vaultsync.ui.theme.Slate950
import com.vaultsync.ui.viewmodel.ConflictsViewModel
import com.vaultsync.ui.viewmodel.DashboardViewModel
import com.vaultsync.ui.viewmodel.DocumentDetailViewModel
import com.vaultsync.ui.viewmodel.DocumentListViewModel
import com.vaultsync.ui.viewmodel.SettingsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.CloudSync)
    object Documents : Screen("documents", "Documents", Icons.Default.Description)
    object Conflicts : Screen("conflicts", "Conflicts", Icons.Default.Warning)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun VaultSyncApp(
    dashboardViewModel: DashboardViewModel,
    documentListViewModel: DocumentListViewModel,
    documentDetailViewModel: DocumentDetailViewModel,
    conflictsViewModel: ConflictsViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }

    val navItems = listOf(
        Screen.Dashboard,
        Screen.Documents,
        Screen.Conflicts,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            if (selectedDocumentId == null) {
                NavigationBar(
                    containerColor = CardBackground,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Slate950,
                                selectedTextColor = AccentCyan,
                                indicatorColor = AccentCyan,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400
                            )
                        )
                    }
                }
            }
        },
        containerColor = Slate950
    ) { innerPadding ->
        if (selectedDocumentId != null) {
            DocumentDetailScreen(
                documentId = selectedDocumentId!!,
                viewModel = documentDetailViewModel,
                onBack = { selectedDocumentId = null },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            when (currentScreen) {
                Screen.Dashboard -> DashboardScreen(
                    viewModel = dashboardViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                Screen.Documents -> DocumentListScreen(
                    viewModel = documentListViewModel,
                    onDocumentClick = { docId -> selectedDocumentId = docId },
                    modifier = Modifier.padding(innerPadding)
                )
                Screen.Conflicts -> ConflictsScreen(
                    viewModel = conflictsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                Screen.Settings -> SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
