package com.graydial.android.domain

/** Every state the privileged-access pipeline can be in. */
enum class PermissionState {
    SHIZUKU_NOT_INSTALLED,
    SHIZUKU_NOT_RUNNING,
    PERMISSION_REQUIRED,
    PERMISSION_DENIED,
    SERVICE_DISCONNECTED,
    UNSUPPORTED,
    /** Permission granted; privileged service not yet bound (binds lazily on first use). */
    PERMISSION_GRANTED,
    READY;

    val canAttemptOperation: Boolean get() = this == READY || this == PERMISSION_GRANTED || this == SERVICE_DISCONNECTED
}

/** Lifecycle of the bound privileged user service. */
enum class ServiceState { NOT_BOUND, BINDING, CONNECTED, DISCONNECTED, UNSUPPORTED }

/** Raw observations from Shizuku and the package manager. */
data class ShizukuSnapshot(
    val installed: Boolean,
    val binderAlive: Boolean,
    val preV11: Boolean,
    val permissionGranted: Boolean,
    val permanentlyDenied: Boolean,
    val deniedThisSession: Boolean,
    val service: ServiceState,
)

/** Pure state resolution, kept free of Android types so it can be unit tested. */
object PermissionStateResolver {
    fun resolve(s: ShizukuSnapshot): PermissionState = when {
        // A running Shizuku binder proves installation even if package visibility hides the app.
        !s.binderAlive && !s.installed -> PermissionState.SHIZUKU_NOT_INSTALLED
        !s.binderAlive -> PermissionState.SHIZUKU_NOT_RUNNING
        s.preV11 -> PermissionState.UNSUPPORTED
        s.service == ServiceState.UNSUPPORTED -> PermissionState.UNSUPPORTED
        s.permissionGranted && s.service == ServiceState.CONNECTED -> PermissionState.READY
        s.permissionGranted && s.service == ServiceState.DISCONNECTED -> PermissionState.SERVICE_DISCONNECTED
        s.permissionGranted -> PermissionState.PERMISSION_GRANTED
        s.permanentlyDenied || s.deniedThisSession -> PermissionState.PERMISSION_DENIED
        else -> PermissionState.PERMISSION_REQUIRED
    }
}
