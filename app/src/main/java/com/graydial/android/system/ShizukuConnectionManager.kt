package com.graydial.android.system

import android.content.Context
import android.content.pm.PackageManager
import com.graydial.android.domain.PermissionState
import com.graydial.android.domain.PermissionStateResolver
import com.graydial.android.domain.SaturationException
import com.graydial.android.domain.ServiceState
import com.graydial.android.domain.ShizukuSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku

/**
 * Tracks Shizuku availability and GrayDial's authorization. GrayDial starts no privileged process
 * of its own; each operation is a single binder call routed through Shizuku.
 */
class ShizukuConnectionManager(private val context: Context) {

    private val lock = Any()
    private val binderAlive = MutableStateFlow(false)
    private val _state = MutableStateFlow(PermissionState.SHIZUKU_NOT_RUNNING)
    val state: StateFlow<PermissionState> = _state.asStateFlow()

    private var serviceState = ServiceState.NOT_BOUND
    private var deniedThisSession = false

    private val binderReceived = Shizuku.OnBinderReceivedListener {
        binderAlive.value = true
        refresh()
    }
    private val binderDead = Shizuku.OnBinderDeadListener {
        binderAlive.value = false
        synchronized(lock) { if (serviceState == ServiceState.CONNECTED) serviceState = ServiceState.NOT_BOUND }
        refresh()
    }
    private val permissionResult = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == PERMISSION_REQUEST_CODE) {
            synchronized(lock) { deniedThisSession = grantResult != PackageManager.PERMISSION_GRANTED }
            refresh()
        }
    }

    fun start() {
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        Shizuku.addRequestPermissionResultListener(permissionResult)
        refresh()
    }

    /** Recomputes [state]. Cheap; safe to call on every resume. */
    fun refresh() {
        val alive = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        binderAlive.value = alive
        val preV11 = alive && runCatching { Shizuku.isPreV11() }.getOrDefault(false)
        val granted = alive && !preV11 &&
            runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)
        val permanentlyDenied = alive && !preV11 && !granted &&
            runCatching { Shizuku.shouldShowRequestPermissionRationale() }.getOrDefault(false)
        val snapshot = synchronized(lock) {
            if (!granted && serviceState == ServiceState.CONNECTED) serviceState = ServiceState.NOT_BOUND
            ShizukuSnapshot(
                installed = isShizukuInstalled(),
                binderAlive = alive,
                preV11 = preV11,
                permissionGranted = granted,
                permanentlyDenied = permanentlyDenied,
                deniedThisSession = deniedThisSession,
                service = serviceState,
            )
        }
        _state.value = PermissionStateResolver.resolve(snapshot)
    }

    /** Shows Shizuku's own permission dialog. Returns false if Shizuku cannot be asked. */
    fun requestPermission(): Boolean {
        refresh()
        return when (_state.value) {
            PermissionState.PERMISSION_REQUIRED, PermissionState.PERMISSION_DENIED -> runCatching {
                if (Shizuku.shouldShowRequestPermissionRationale()) return false // "Deny and don't ask again"
                Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
                true
            }.getOrDefault(false)
            else -> false
        }
    }

    fun isShizukuInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    /** Throws a [SaturationException] unless Shizuku is running and GrayDial is authorized. */
    suspend fun ensureReady() {
        if (!binderAlive.value) {
            // On a cold start (e.g. from the tile) Shizuku delivers its binder asynchronously.
            withTimeoutOrNull(BINDER_WAIT_MS) { binderAlive.first { it } }
        }
        refresh()
        when (_state.value) {
            PermissionState.SHIZUKU_NOT_INSTALLED -> throw SaturationException.ShizukuNotInstalled()
            PermissionState.SHIZUKU_NOT_RUNNING -> throw SaturationException.ShizukuNotRunning()
            PermissionState.PERMISSION_REQUIRED -> throw SaturationException.PermissionRequired()
            PermissionState.PERMISSION_DENIED -> throw SaturationException.PermissionDenied()
            PermissionState.UNSUPPORTED -> throw SaturationException.Unsupported(
                if (runCatching { Shizuku.isPreV11() }.getOrDefault(false)) "Shizuku is too old; update Shizuku"
                else "the system operation is unavailable",
            )
            else -> Unit
        }
    }

    /** A privileged call just succeeded. */
    fun markWorking() {
        synchronized(lock) { serviceState = ServiceState.CONNECTED }
        refresh()
    }

    /** The system reported the operation is unavailable on this device. */
    fun markUnsupported() {
        synchronized(lock) { serviceState = ServiceState.UNSUPPORTED }
        refresh()
    }

    companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        private const val PERMISSION_REQUEST_CODE = 0x6D1A
        private const val BINDER_WAIT_MS = 3_000L
    }
}
