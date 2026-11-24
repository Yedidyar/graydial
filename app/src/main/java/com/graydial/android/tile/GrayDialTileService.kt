package com.graydial.android.tile

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.graydial.android.R
import com.graydial.android.appContainer
import com.graydial.android.data.GrayDialPreferences
import com.graydial.android.domain.ApplyOutcome
import com.graydial.android.domain.GrayDialController
import com.graydial.android.domain.GrayscaleIntensity
import com.graydial.android.domain.SaturationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Quick Settings tile. Shows only the last *successfully applied* state; while an operation runs
 * the tile says so instead of optimistically flipping.
 */
class GrayDialTileService : TileService() {

    private val scope = MainScope()
    private var refreshJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        refreshJob?.cancel()
        refreshJob = scope.launch {
            val container = appContainer
            withContext(Dispatchers.Default) { container.controller.reconcileWithBoot() }
            render(container.preferences.current(), container.controller.lastOutcome.value, busy = false)
        }
    }

    override fun onClick() {
        super.onClick()
        val container = appContainer
        val tile = qsTile ?: return
        tile.subtitle = getString(R.string.tile_applying)
        tile.updateTile()
        // Run in the app scope so the operation completes even if the panel closes.
        container.appScope.launch {
            container.controller.toggle()
            requestRefresh(applicationContext)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun render(p: GrayDialPreferences, outcome: ApplyOutcome?, busy: Boolean) {
        val tile = qsTile ?: return
        val active = GrayDialController.isActive(p)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.subtitle = when {
            busy -> getString(R.string.tile_applying)
            outcome is ApplyOutcome.Failed -> failureLabel(outcome.error)
            active -> getString(
                R.string.tile_percent,
                GrayscaleIntensity.fromSaturation(p.lastAppliedSaturation!!).percent,
            )
            else -> getString(R.string.tile_off)
        }
        tile.updateTile()
    }

    private fun failureLabel(error: Throwable): String = when (error) {
        is SaturationException.ShizukuNotInstalled,
        is SaturationException.ShizukuNotRunning,
        is SaturationException.PermissionRequired,
        is SaturationException.PermissionDenied -> getString(R.string.tile_setup_needed)
        is SaturationException.Unsupported -> getString(R.string.tile_unsupported)
        else -> getString(R.string.tile_failed)
    }

    companion object {
        fun requestRefresh(context: Context) {
            runCatching {
                requestListeningState(context, ComponentName(context, GrayDialTileService::class.java))
            }
        }
    }
}
