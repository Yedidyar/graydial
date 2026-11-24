package com.graydial.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import com.graydial.android.domain.PermissionState

data class PermissionCopy(val title: String, val body: String, val action: String?)

fun PermissionState.copy(): PermissionCopy = when (this) {
    PermissionState.SHIZUKU_NOT_INSTALLED -> PermissionCopy(
        "Shizuku not installed",
        "GrayDial uses Shizuku to change the system color filter without root. Install Shizuku, then follow Setup.",
        "Get Shizuku",
    )
    PermissionState.SHIZUKU_NOT_RUNNING -> PermissionCopy(
        "Shizuku is not running",
        "Open Shizuku and start it via Wireless debugging. Shizuku must be started again after every restart.",
        "Open Shizuku",
    )
    PermissionState.PERMISSION_REQUIRED -> PermissionCopy(
        "Access required",
        "Allow GrayDial in the Shizuku dialog. GrayDial only ever changes display saturation.",
        "Grant access",
    )
    PermissionState.PERMISSION_DENIED -> PermissionCopy(
        "Access denied",
        "If no dialog appears, open Shizuku › Authorized applications and allow GrayDial.",
        "Try again",
    )
    PermissionState.SERVICE_DISCONNECTED -> PermissionCopy(
        "Connection lost",
        "Shizuku stopped responding. GrayDial retries on the next change.",
        "Reconnect",
    )
    PermissionState.UNSUPPORTED -> PermissionCopy(
        "Not supported",
        "This device or Shizuku version does not support system saturation control. Update Shizuku; on some builds the manufacturer blocks this operation.",
        null,
    )
    PermissionState.PERMISSION_GRANTED -> PermissionCopy("Ready", "Shizuku access granted.", null)
    PermissionState.READY -> PermissionCopy("Ready", "Connected to the system color service through Shizuku.", null)
}

@Composable
fun PermissionCard(state: PermissionState, onAction: () -> Unit, modifier: Modifier = Modifier) {
    val copy = state.copy()
    val ok = state == PermissionState.READY || state == PermissionState.PERMISSION_GRANTED
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dot = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                Spacer(Modifier.size(8.dp).clip(CircleShape).background(dot))
                Spacer(Modifier.size(10.dp))
                Text(copy.title, style = MaterialTheme.typography.titleSmall)
            }
            if (!ok) {
                Text(copy.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                copy.action?.let { FilledTonalButton(onClick = onAction) { Text(it) } }
            }
        }
    }
}
