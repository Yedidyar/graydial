package com.graydial.android.domain

class FakeSaturationController : SaturationController {
    val calls = mutableListOf<Int>()
    var failWith: Throwable? = null
    var activated: Result<Boolean>? = null // null = mirror last call
    private var current = 100

    override suspend fun setSaturation(saturation: Int): Result<Unit> {
        if (!GrayscaleIntensity.isValidSaturation(saturation)) return Result.failure(SaturationException.InvalidValue(saturation))
        calls += saturation
        failWith?.let { return Result.failure(it) }
        current = saturation
        return Result.success(Unit)
    }

    override suspend fun isSaturationActivated(): Result<Boolean> = activated ?: Result.success(current < 100)
}
