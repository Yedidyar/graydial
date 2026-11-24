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
import com.graydial.android.domain.SetupAction
import com.graydial.android.domain.SetupStep
import com.graydial.android.domain.SetupWizard

data class StepCopy(val title: String, val body: String, val button: String?)

fun SetupStep.copy(denied: Boolean = false): StepCopy = when (this) {
    SetupStep.INSTALL -> StepCopy(
        "Install Shizuku",
        "GrayDial changes the system color filter through Shizuku, a free helper app. No root needed.",
        "Get Shizuku",
    )
    SetupStep.START -> StepCopy(
        "Start Shizuku",
        "Open Shizuku and start it with Wireless debugging. Shizuku must be started again after every restart.",
        "Open Shizuku",
    )
    SetupStep.GRANT -> StepCopy(
        if (denied) "Access denied" else "Allow GrayDial",
        if (denied) "If no dialog appears, open Shizuku \u203a Authorized applications and allow GrayDial."
        else "Allow GrayDial in the Shizuku dialog. GrayDial only ever changes display saturation.",
        if (denied) "Try again" else "Grant access",
    )
    SetupStep.RECONNECT -> StepCopy(
        "Connection lost",
        "Shizuku stopped responding. Make sure it is running, then reconnect.",
        "Reconnect",
    )
    SetupStep.UNSUPPORTED -> StepCopy(
        "Not supported",
        "This device or Shizuku version does not support system saturation control. Update Shizuku; on some builds the manufacturer blocks this operation.",
        null,
    )
    SetupStep.DONE -> StepCopy("Ready", "Connected to the system color service through Shizuku.", null)
}

@Composable
fun SetupWizardCard(state: PermissionState, onAction: (SetupAction) -> Unit, modifier: Modifier = Modifier) {
    val step = SetupWizard.stepFor(state)
    val copy = step.copy(denied = state == PermissionState.PERMISSION_DENIED)
    val done = step == SetupStep.DONE
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dot = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                Spacer(Modifier.size(8.dp).clip(CircleShape).background(dot))
                Spacer(Modifier.size(10.dp))
                Text(copy.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                step.number?.let {
                    Text(
                        "Step $it of ${SetupWizard.TOTAL_STEPS}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!done) {
                Text(copy.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                copy.button?.let { FilledTonalButton(onClick = { onAction(SetupWizard.actionFor(step)) }) { Text(it) } }
            }
        }
    }
}
