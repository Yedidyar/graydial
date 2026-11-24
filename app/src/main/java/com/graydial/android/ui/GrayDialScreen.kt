package com.graydial.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.graydial.android.domain.ApplyOutcome
import com.graydial.android.domain.PermissionState
import com.graydial.android.domain.Preset
import com.graydial.android.ui.components.PermissionCard
import com.graydial.android.ui.components.SpectrumPreview
import kotlin.math.roundToInt

data class ScreenActions(
    val onEnabledChanged: (Boolean) -> Unit,
    val onIntensityChanged: (Int) -> Unit,
    val onPreset: (Preset) -> Unit,
    val onCustomPreset: (Int) -> Unit,
    val onSavePreset: () -> Unit,
    val onRemovePreset: (Int) -> Unit,
    val onRestore: () -> Unit,
    val onReapply: () -> Unit,
    val onDismissRestartNotice: () -> Unit,
    val onPermissionAction: (PermissionState) -> Unit,
    val onOpenShizuku: () -> Unit,
    val versionName: String,
)

@Composable
fun GrayDialScreen(state: GrayDialUiState, actions: ScreenActions) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showSettings) { showSettings = false }
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (showSettings) {
                    SettingsContent(actions, onClose = { showSettings = false })
                } else {
                    MainContent(state, actions, onOpenSettings = { showSettings = true })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainContent(state: GrayDialUiState, actions: ScreenActions, onOpenSettings: () -> Unit) {
    Header(onOpenSettings)

    PermissionCard(state.permission, onAction = { actions.onPermissionAction(state.permission) })

    if (state.resetByRestart) {
        Notice(
            text = "Your device restarted, which cleared the grayscale filter. GrayDial does not reapply it automatically.",
            primary = "Reapply" to { actions.onEnabledChanged(true) },
            secondary = "Dismiss" to actions.onDismissRestartNotice,
        )
    }

    // Master switch
    Row(
        Modifier.fillMaxWidth().semantics { stateDescription = if (state.enabled) "On" else "Off" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Grayscale", style = MaterialTheme.typography.titleMedium)
            Text(
                if (state.enabled) "On" else "Off · your intensity is kept",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.working) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.size(12.dp))
        }
        Switch(checked = state.enabled, onCheckedChange = actions.onEnabledChanged)
    }

    // Big percentage
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${state.intensity}%",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.semantics { contentDescription = "${state.intensity} percent grayscale" },
        )
        Text(
            "GRAYSCALE INTENSITY",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    SpectrumPreview(state.intensity)

    Slider(
        value = state.intensity.toFloat(),
        onValueChange = { actions.onIntensityChanged(it.roundToInt()) },
        valueRange = 0f..100f,
        modifier = Modifier.semantics { contentDescription = "Grayscale intensity" },
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Preset.entries.forEach { preset ->
            FilterChip(
                selected = state.selectedPreset == preset,
                onClick = { actions.onPreset(preset) },
                label = { Text("${preset.label} · ${preset.percent}%") },
            )
        }
        state.customPresets.forEach { percent ->
            InputChip(
                selected = state.intensity == percent,
                onClick = { actions.onCustomPreset(percent) },
                label = { Text("$percent%") },
                trailingIcon = {
                    Text(
                        "×",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .clickable(onClickLabel = "Remove $percent% preset") { actions.onRemovePreset(percent) }
                            .padding(horizontal = 4.dp),
                    )
                },
            )
        }
        if (state.canSaveCurrentAsPreset) {
            AssistChip(
                onClick = actions.onSavePreset,
                label = { Text("+ Save ${state.intensity}%") },
            )
        }
    }

    StatusBlock(state)

    OutlinedButton(
        onClick = actions.onRestore,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text("Restore original colors")
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("GrayDial", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Less color. More focus.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onOpenSettings) { Text("Settings") }
    }
}

@Composable
private fun StatusBlock(state: GrayDialUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text("SYSTEM STATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(state.appliedDescription(), style = MaterialTheme.typography.bodyMedium)
        val outcome = state.lastOutcome
        if (outcome is ApplyOutcome.Failed && !state.working) {
            Text(
                "Last change was not applied. ${outcome.error.message ?: "Unknown error."}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else if (state.pendingChange && !state.working) {
            Text(
                "Selection not yet applied.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Notice(text: String, primary: Pair<String, () -> Unit>, secondary: Pair<String, () -> Unit>) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = primary.second) { Text(primary.first) }
                TextButton(onClick = secondary.second) { Text(secondary.first) }
            }
        }
    }
}

@Composable
private fun SettingsContent(actions: ScreenActions, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onClose) { Text("Done") }
    }
    Section(
        "Setup (no computer needed)",
        """
        1. Install Shizuku.
        2. Connect to Wi-Fi. In Settings › About phone, tap the OS version (MIUI/HyperOS version) 7 times to enable Developer options.
        3. In Developer options, turn on Wireless debugging.
        4. Open Shizuku › Start via Wireless debugging › Pairing, and enter the pairing code shown by Wireless debugging › Pair device with pairing code (use split-screen or the notification).
        5. Start Shizuku, return to GrayDial and tap “Grant access”.
        6. After Shizuku shows “running”, you may turn Wireless debugging off. GrayDial keeps working until Shizuku stops (always at restart). Whether Shizuku survives this on your device should be confirmed once.
        """.trimIndent(),
    )
    OutlinedButton(onClick = actions.onOpenShizuku, modifier = Modifier.fillMaxWidth()) { Text("Open Shizuku") }
    Section(
        "Quick Settings tile",
        "Edit your Quick Settings panel and add the GrayDial tile. Tap to toggle; long-press to open GrayDial. The tile shows only the last state the system accepted.",
    )
    Section(
        "After a restart",
        "Android clears the filter on every restart and Shizuku stops. GrayDial never reapplies the filter on its own; it tells you and lets you reapply.",
    )
    Section(
        "Privacy & security",
        "GrayDial has no internet permission, no analytics and no accounts. Preferences stay on this device. Its privileged helper can only set display saturation (0–100). Note that Shizuku itself holds broader shell-level privileges; only authorize apps you trust in Shizuku.",
    )
    Text(
        "GrayDial ${actions.versionName}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
