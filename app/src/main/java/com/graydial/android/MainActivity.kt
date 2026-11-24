package com.graydial.android

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.graydial.android.domain.SetupAction
import com.graydial.android.system.ShizukuConnectionManager
import com.graydial.android.ui.GrayDialScreen
import com.graydial.android.ui.GrayDialViewModel
import com.graydial.android.ui.ScreenActions
import com.graydial.android.ui.theme.GrayDialTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GrayDialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Incoming intents (launcher, QS long-press) carry no data GrayDial reads; nothing to validate.
        val actions = ScreenActions(
            onEnabledChanged = viewModel::onEnabledChanged,
            onIntensityChanged = viewModel::onIntensityChanged,
            onPreset = viewModel::onPresetSelected,
            onCustomPreset = viewModel::onCustomPresetSelected,
            onSavePreset = viewModel::onSaveCurrentAsPreset,
            onRemovePreset = viewModel::onRemoveCustomPreset,
            onRestore = viewModel::onRestoreOriginalColors,
            onReapply = viewModel::onReapply,
            onDismissRestartNotice = viewModel::onDismissRestartNotice,
            onSetupAction = ::onSetupAction,
            onOpenShizuku = ::openShizuku,
            versionName = BuildConfig.VERSION_NAME,
        )
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            GrayDialTheme { GrayDialScreen(state, actions) }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun onSetupAction(action: SetupAction) {
        when (action) {
            SetupAction.GET_SHIZUKU -> openShizukuStore()
            SetupAction.OPEN_SHIZUKU -> openShizuku()
            SetupAction.REQUEST_PERMISSION -> if (!viewModel.onRequestPermission()) {
                toast("Open Shizuku \u203a Authorized applications and allow GrayDial.")
                openShizuku()
            }
            SetupAction.RECONNECT -> viewModel.onReapply()
            SetupAction.NONE -> Unit
        }
    }

    private fun openShizuku() {
        val launch = packageManager.getLaunchIntentForPackage(ShizukuConnectionManager.SHIZUKU_PACKAGE)
        if (launch != null) startActivity(launch) else openShizukuStore()
    }

    /**
     * Opens Shizuku in Google Play explicitly (a plain market:// link is claimed by Xiaomi GetApps
     * on HyperOS), then falls back to the official page. GrayDial itself makes no network requests.
     */
    private fun openShizukuStore() {
        val play = Intent(Intent.ACTION_VIEW, "market://details?id=${ShizukuConnectionManager.SHIZUKU_PACKAGE}".toUri())
            .setPackage(PLAY_STORE_PACKAGE)
        val web = Intent(Intent.ACTION_VIEW, "https://shizuku.rikka.app/download/".toUri())
        try {
            startActivity(play)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(web)
            } catch (_: ActivityNotFoundException) {
                toast("Install Shizuku from shizuku.rikka.app")
            }
        }
    }

    private fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private companion object {
        const val PLAY_STORE_PACKAGE = "com.android.vending"
    }
}
