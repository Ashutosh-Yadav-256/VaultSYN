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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultsync.domain.model.Conflict
import com.vaultsync.domain.model.ConflictResolution
import com.vaultsync.ui.theme.AccentCyan
import com.vaultsync.ui.theme.BorderSubtle
import com.vaultsync.ui.theme.CardBackground
import com.vaultsync.ui.theme.ErrorRose
import com.vaultsync.ui.theme.Slate400
import com.vaultsync.ui.theme.Slate800
import com.vaultsync.ui.theme.Slate950
import com.vaultsync.ui.theme.VerifiedEmerald
import com.vaultsync.ui.viewmodel.ConflictsViewModel

@Composable
fun ConflictsScreen(
    viewModel: ConflictsViewModel,
    modifier: Modifier = Modifier
) {
    val conflicts by viewModel.conflicts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Conflicts",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Text(
            text = "Resolve divergent file versions before synchronization",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (conflicts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VerifiedEmerald,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Zero Conflicts Detected",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All local and destination files match SHA-256 digests",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(conflicts, key = { it.id }) { conflict ->
                    ConflictCard(
                        conflict = conflict,
                        onKeepLocal = { viewModel.resolve(conflict.id, ConflictResolution.KEEP_LOCAL) },
                        onKeepRemote = { viewModel.resolve(conflict.id, ConflictResolution.KEEP_REMOTE) },
                        onKeepBoth = { viewModel.resolve(conflict.id, ConflictResolution.KEEP_BOTH) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConflictCard(
    conflict: Conflict,
    onKeepLocal: () -> Unit,
    onKeepRemote: () -> Unit,
    onKeepBoth: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, ErrorRose.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ErrorRose,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = conflict.documentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Versions comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(10.dp)
                ) {
                    Text(text = "Local Version", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SHA: ${conflict.localHash.take(12)}...",
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(10.dp)
                ) {
                    Text(text = "Remote Version", color = ErrorRose, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SHA: ${conflict.remoteHash.take(12)}...",
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resolution Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onKeepLocal,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Keep Local", fontSize = 11.sp)
                }

                Button(
                    onClick = onKeepRemote,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Keep Remote", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onKeepBoth,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Keep Both", fontSize = 11.sp, color = AccentCyan)
                }
            }
        }
    }
}
