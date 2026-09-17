package com.vaultsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultsync.ui.theme.AccentCyan
import com.vaultsync.ui.theme.BorderSubtle
import com.vaultsync.ui.theme.CardBackground
import com.vaultsync.ui.theme.ErrorRose
import com.vaultsync.ui.theme.Slate400
import com.vaultsync.ui.theme.Slate950
import com.vaultsync.ui.theme.VerifiedEmerald
import com.vaultsync.ui.theme.WarningAmber
import com.vaultsync.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.statusSummary.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(20.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = "VaultSync Logo",
                tint = AccentCyan,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "VaultSync",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Text(
                    text = "Secure Local-First Document Manager",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Documents",
                value = summary.totalDocuments.toString(),
                icon = Icons.Default.Description,
                tint = AccentCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Synced",
                value = summary.syncedCount.toString(),
                icon = Icons.Default.CloudSync,
                tint = VerifiedEmerald,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Pending",
                value = summary.pendingCount.toString(),
                icon = Icons.Default.PendingActions,
                tint = WarningAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Conflicts",
                value = summary.conflictCount.toString(),
                icon = Icons.Default.Warning,
                tint = if (summary.conflictCount > 0) ErrorRose else Slate400,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { viewModel.syncNow() },
            enabled = !summary.isSyncing,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                contentColor = Slate950
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (summary.isSyncing) {
                CircularProgressIndicator(
                    color = Slate950,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text("Syncing Vault...", fontWeight = FontWeight.Bold)
            } else {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Sync Now", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "HARDWARE-BACKED ENCRYPTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerifiedEmerald,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "AES-256-GCM + SHA-256 Checksums",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Documents are encrypted at rest with keys protected by the Android Keystore hardware enclave.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, color = Slate400, fontSize = 13.sp)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
