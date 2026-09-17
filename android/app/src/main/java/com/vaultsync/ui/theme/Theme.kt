package com.vaultsync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Slate950,
    primaryContainer = AccentCyanDark,
    onPrimaryContainer = Slate100,
    secondary = VerifiedEmerald,
    onSecondary = Slate950,
    background = Slate950,
    onBackground = Slate100,
    surface = CardBackground,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = BorderSubtle,
    error = ErrorRose,
    onError = Slate950
)

@Composable
fun VaultSyncTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
