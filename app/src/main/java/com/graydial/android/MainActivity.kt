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
import com.graydial.android.domain.PermissionState
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
            onRestore = viewModel::onRestoreOriginalColors,
            onReapply = viewModel::onReapply,
            onDismissRestartNotice = viewModel::onDismissRestartNotice,
            onPermissionAction = ::onPermissionAction,
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

    private fun onPermissionAction(state: PermissionState) {
        when (state) {
            PermissionState.SHIZUKU_NOT_INSTALLED -> openShizukuStore()
            PermissionState.SHIZUKU_NOT_RUNNING -> openShizuku()
            PermissionState.PERMISSION_REQUIRED, PermissionState.PERMISSION_DENIED -> {
                if (!viewModel.onRequestPermission()) {
                    toast("Open Shizuku › Authorized applications and allow GrayDial.")
                    openShizuku()
                }
            }
            PermissionState.SERVICE_DISCONNECTED -> viewModel.onReapply()
            else -> Unit
        }
    }

    private fun openShizuku() {
        val launch = packageManager.getLaunchIntentForPackage(ShizukuConnectionManager.SHIZUKU_PACKAGE)
        if (launch != null) startActivity(launch) else openShizukuStore()
    }

    /** Opens the store / official page in another app. GrayDial itself makes no network requests. */
    private fun openShizukuStore() {
        val market = Intent(Intent.ACTION_VIEW, "market://details?id=${ShizukuConnectionManager.SHIZUKU_PACKAGE}".toUri())
        val web = Intent(Intent.ACTION_VIEW, "https://shizuku.rikka.app/download/".toUri())
        try {
            startActivity(market)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(web)
            } catch (_: ActivityNotFoundException) {
                toast("Install Shizuku from shizuku.rikka.app")
            }
        }
    }

    private fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
