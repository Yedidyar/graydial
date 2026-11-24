package com.graydial.android.domain

/** Narrow abstraction over the privileged display-saturation mechanism. */
interface SaturationController {
    /** Requests global saturation 0..100. Success means the command succeeded, not that the display was inspected. */
    suspend fun setSaturation(saturation: Int): Result<Unit>

    /** Restores normal colors (saturation 100). */
    suspend fun restoreDefaults(): Result<Unit> = setSaturation(NORMAL_SATURATION)

    /**
     * Best-effort read-back. Android only exposes whether the global saturation transform is
     * active (true when saturation < 100); the exact level cannot be queried.
     */
    suspend fun isSaturationActivated(): Result<Boolean>

    companion object {
        const val NORMAL_SATURATION = 100
    }
}

/** Failures surfaced to the user with actionable messages. */
sealed class SaturationException(message: String) : Exception(message) {
    class InvalidValue(value: Int) : SaturationException("Rejected invalid saturation value $value (allowed 0–100).")
    class ShizukuNotInstalled : SaturationException("Shizuku is not installed. Install Shizuku, then start it with wireless debugging.")
    class ShizukuNotRunning : SaturationException("Shizuku is not running. Open Shizuku and start it, then return to GrayDial.")
    class PermissionRequired : SaturationException("GrayDial needs Shizuku permission. Tap “Grant access”.")
    class PermissionDenied : SaturationException("Shizuku permission was denied. Allow GrayDial in the Shizuku app’s authorized apps list.")
    class ServiceUnavailable(detail: String) : SaturationException("The privileged service is unavailable: $detail")
    class Unsupported(detail: String) : SaturationException("This device does not support the system saturation control: $detail")
    class Timeout : SaturationException("The system did not respond in time. Check that Shizuku is still running and try again.")
    class CommandFailed(detail: String) : SaturationException("The system rejected the saturation change: $detail")
    class SystemPermissionDenied(detail: String) : SaturationException(
        "The system refused the change for the shell user ($detail). Some manufacturer builds restrict this operation.",
    )
}
