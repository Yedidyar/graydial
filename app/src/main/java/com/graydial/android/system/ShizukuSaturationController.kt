package com.graydial.android.system

import android.os.RemoteException
import com.graydial.android.domain.GrayscaleIntensity
import com.graydial.android.domain.SaturationController
import com.graydial.android.domain.SaturationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** [SaturationController] backed by Shizuku and the system color_display service. */
class ShizukuSaturationController(
    private val connection: ShizukuConnectionManager,
    private val colorDisplay: ShizukuColorDisplay = ShizukuColorDisplay(),
) : SaturationController {

    override suspend fun setSaturation(saturation: Int): Result<Unit> {
        if (!GrayscaleIntensity.isValidSaturation(saturation)) {
            return Result.failure(SaturationException.InvalidValue(saturation))
        }
        return call {
            when (val code = colorDisplay.setSaturation(saturation)) {
                0 -> connection.markWorking()
                null -> throw SaturationException.Timeout()
                else -> throw SaturationException.CommandFailed("color_display returned $code")
            }
        }
    }

    override suspend fun isSaturationActivated(): Result<Boolean> = call {
        colorDisplay.isSaturationActivated()
            ?: throw SaturationException.Unsupported("saturation state is not readable on this device")
    }

    private suspend fun <T> call(block: () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            connection.ensureReady()
            // Deliberately outside structured concurrency: a hung binder call must not block us.
            val deferred = CoroutineScope(Dispatchers.IO).async { block() }
            val result = withTimeoutOrNull(OPERATION_TIMEOUT_MS) { deferred.await() }
                ?: run { deferred.cancel(); throw SaturationException.Timeout() }
            Result.success(result)
        } catch (e: SaturationException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(SaturationException.SystemPermissionDenied(e.message ?: "SecurityException"))
        } catch (e: UnsupportedOperationException) {
            connection.markUnsupported()
            Result.failure(SaturationException.Unsupported(e.message ?: "unsupported"))
        } catch (e: RemoteException) {
            connection.refresh()
            Result.failure(SaturationException.ShizukuNotRunning())
        } catch (e: IllegalStateException) {
            // Shizuku throws this when its binder is not available.
            connection.refresh()
            Result.failure(SaturationException.ShizukuNotRunning())
        }
    }

    private companion object {
        const val OPERATION_TIMEOUT_MS = 7_000L
    }
}
