package com.graydial.android.domain

/** Where the user is in first-time setup. Derived purely from [PermissionState]. */
enum class SetupStep(val number: Int?) {
    INSTALL(1),
    START(2),
    GRANT(3),
    /** Shizuku was authorized but stopped answering; one tap re-sends the requested state. */
    RECONNECT(null),
    UNSUPPORTED(null),
    DONE(null),
}

/** What the single primary button of a step does. */
enum class SetupAction { GET_SHIZUKU, OPEN_SHIZUKU, REQUEST_PERMISSION, RECONNECT, NONE }

object SetupWizard {
    const val TOTAL_STEPS = 3

    fun stepFor(state: PermissionState): SetupStep = when (state) {
        PermissionState.SHIZUKU_NOT_INSTALLED -> SetupStep.INSTALL
        PermissionState.SHIZUKU_NOT_RUNNING -> SetupStep.START
        PermissionState.PERMISSION_REQUIRED, PermissionState.PERMISSION_DENIED -> SetupStep.GRANT
        PermissionState.SERVICE_DISCONNECTED -> SetupStep.RECONNECT
        PermissionState.UNSUPPORTED -> SetupStep.UNSUPPORTED
        PermissionState.PERMISSION_GRANTED, PermissionState.READY -> SetupStep.DONE
    }

    fun actionFor(step: SetupStep): SetupAction = when (step) {
        SetupStep.INSTALL -> SetupAction.GET_SHIZUKU
        SetupStep.START -> SetupAction.OPEN_SHIZUKU
        SetupStep.GRANT -> SetupAction.REQUEST_PERMISSION
        SetupStep.RECONNECT -> SetupAction.RECONNECT
        SetupStep.UNSUPPORTED, SetupStep.DONE -> SetupAction.NONE
    }

    /** Setup is shown until access works; afterwards only a one-line status remains. */
    fun isComplete(state: PermissionState): Boolean = stepFor(state) == SetupStep.DONE
}
