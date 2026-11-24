package com.graydial.android.system

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.ResultReceiver
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * The only privileged code in GrayDial. It talks to the system `color_display` service through
 * Shizuku's binder wrapper, so the system sees the call as coming from the shell user.
 *
 * Exactly two fixed operations exist:
 *  - `set-saturation <0..100>` via the binder shell-command transaction (the same entry point as
 *    `adb shell cmd color_display`). Arguments are a constant plus a validated Int; no shell process
 *    is started and no strings from outside are passed.
 *  - Read whether global saturation is active, via the service's dump output.
 *
 * ColorDisplayService validates the level again (0..100) on its side.
 *
 * Why not a Shizuku UserService: on Android 16 / HyperOS 3.0 with Shizuku 13.6.0, starting a user
 * service crashes inside LoadedApk.makeApplicationInner before any app code runs.
 */
class ShizukuColorDisplay {

    /** Result code of the system command: 0 = success, -1 = rejected. Null = no result in time. */
    fun setSaturation(saturation: Int): Int? {
        require(saturation in 0..100) { "Saturation must be in 0..100, was $saturation" }
        return shellCommand(arrayOf(SET_SATURATION, saturation.toString()))
    }

    /** True/false from the service's dump; null if it could not be determined. */
    fun isSaturationActivated(): Boolean? {
        val pipe = ParcelFileDescriptor.createPipe()
        var result: Boolean? = null
        val reader = thread(name = "graydial-dump") {
            runCatching {
                ParcelFileDescriptor.AutoCloseInputStream(pipe[0]).bufferedReader().use { r ->
                    var inSection = false
                    var lines = 0
                    while (lines++ < MAX_DUMP_LINES) {
                        val line = r.readLine() ?: break
                        if (line.trim() == "Global saturation:") inSection = true
                        else if (inSection) {
                            val v = line.trim().removePrefix("Activated:").trim()
                            result = when (v) { "true" -> true; "false" -> false; else -> null }
                            break
                        }
                    }
                }
            }
        }
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeFileDescriptor(pipe[1].fileDescriptor)
            data.writeStringArray(emptyArray())
            // Dump is synchronous and streams into the pipe, so it is read concurrently above.
            colorDisplay().transact(IBinder.DUMP_TRANSACTION, data, reply, 0)
        } finally {
            data.recycle()
            reply.recycle()
            pipe[1].close()
        }
        // Shizuku may keep its copy of the pipe open, so never wait for EOF beyond the deadline.
        reader.join(DUMP_READ_TIMEOUT_MS)
        return result
    }

    private fun shellCommand(args: Array<String>): Int? {
        // Output is discarded: Shizuku can hold its copy of the descriptors open, so reading to
        // end-of-stream may never finish. The result code is delivered through the receiver.
        val devNull = File("/dev/null")
        val input = ParcelFileDescriptor.open(devNull, ParcelFileDescriptor.MODE_READ_ONLY)
        val output = ParcelFileDescriptor.open(devNull, ParcelFileDescriptor.MODE_WRITE_ONLY)
        val latch = CountDownLatch(1)
        var code: Int? = null
        val handlerThread = HandlerThread("graydial-result").apply { start() }
        val receiver = object : ResultReceiver(Handler(handlerThread.looper)) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                code = resultCode
                latch.countDown()
            }
        }
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeFileDescriptor(input.fileDescriptor)
            data.writeFileDescriptor(output.fileDescriptor)
            data.writeFileDescriptor(output.fileDescriptor)
            data.writeStringArray(args)
            data.writeStrongBinder(null) // no ShellCallback: the command never opens files
            receiver.writeToParcel(data, 0)
            colorDisplay().transact(SHELL_COMMAND_TRANSACTION, data, reply, 0)
            latch.await(RESULT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            return code
        } finally {
            data.recycle()
            reply.recycle()
            input.close()
            output.close()
            handlerThread.quitSafely()
        }
    }

    private fun colorDisplay(): IBinder {
        val binder = SystemServiceHelper.getSystemService(SERVICE)
            ?: throw UnsupportedOperationException("system service '$SERVICE' not found")
        return ShizukuBinderWrapper(binder)
    }

    private companion object {
        const val SERVICE = "color_display"
        const val SET_SATURATION = "set-saturation"

        /** IBinder.SHELL_COMMAND_TRANSACTION ('_CMD'), stable since Android 7; hidden in the SDK. */
        const val SHELL_COMMAND_TRANSACTION =
            ('_'.code shl 24) or ('C'.code shl 16) or ('M'.code shl 8) or 'D'.code

        const val RESULT_TIMEOUT_MS = 5_000L
        const val DUMP_READ_TIMEOUT_MS = 2_000L
        const val MAX_DUMP_LINES = 2_000
    }
}
