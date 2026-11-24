package com.graydial.android

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.graydial.android.domain.ApplyOutcome
import com.graydial.android.domain.Verification
import com.graydial.android.tile.GrayDialTileService
import kotlinx.coroutines.launch

/**
 * Invisible trampoline for the launcher's long-press shortcuts. It reads only the intent's
 * action string (two fixed values, anything else is ignored) and runs the same controller
 * operations as the tile. The toast reports what the system actually confirmed.
 */
class ShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext
        val container = appContainer
        val on = when (intent?.action) {
            ACTION_GRAYSCALE_ON -> true
            ACTION_RESTORE_COLORS -> false
            else -> null
        }
        if (on != null) {
            container.appScope.launch {
                val outcome = if (on) container.controller.setEnabled(true) else container.controller.restoreOriginalColors()
                GrayDialTileService.requestRefresh(app)
                val message = message(on, outcome)
                android.os.Handler(app.mainLooper).post { Toast.makeText(app, message, Toast.LENGTH_SHORT).show() }
            }
        }
        finish()
    }

    private fun message(on: Boolean, outcome: ApplyOutcome): String = when {
        outcome is ApplyOutcome.Failed -> "Not applied. Open GrayDial to finish setup."
        outcome is ApplyOutcome.Applied && outcome.verification != Verification.CONFIRMED ->
            "Sent, but the system state could not be confirmed."
        on -> "Grayscale on"
        else -> "Original colors restored"
    }

    companion object {
        const val ACTION_GRAYSCALE_ON = "io.github.yedidyar.graydial.action.GRAYSCALE_ON"
        const val ACTION_RESTORE_COLORS = "io.github.yedidyar.graydial.action.RESTORE_COLORS"
    }
}
