package com.graydial.android.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * Debounces rapid input (slider drags) and runs [action] sequentially for the latest value only.
 * Actions are never cancelled mid-flight, so a privileged call that succeeded is always recorded.
 */
@OptIn(FlowPreview::class)
class LatestValueApplier<T>(
    scope: CoroutineScope,
    debounceMillis: Long,
    private val action: suspend (T) -> Unit,
) {
    private val input = MutableSharedFlow<T>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        scope.launch {
            input.debounce(debounceMillis).conflate().collect { action(it) }
        }
    }

    fun submit(value: T) {
        input.tryEmit(value)
    }
}
