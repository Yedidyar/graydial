package com.graydial.android.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupWizardTest {
    @Test fun `every permission state maps to a step`() {
        PermissionState.entries.forEach { SetupWizard.stepFor(it) }
    }

    @Test fun `steps progress install start grant done`() {
        val path = listOf(
            PermissionState.SHIZUKU_NOT_INSTALLED, PermissionState.SHIZUKU_NOT_RUNNING,
            PermissionState.PERMISSION_REQUIRED, PermissionState.READY,
        ).map(SetupWizard::stepFor)
        assertEquals(listOf(SetupStep.INSTALL, SetupStep.START, SetupStep.GRANT, SetupStep.DONE), path)
    }

    @Test fun `denied stays on the grant step`() =
        assertEquals(SetupStep.GRANT, SetupWizard.stepFor(PermissionState.PERMISSION_DENIED))

    @Test fun `granted but unbound counts as done`() =
        assertTrue(SetupWizard.isComplete(PermissionState.PERMISSION_GRANTED))

    @Test fun `setup is skipped entirely when ready`() {
        assertTrue(SetupWizard.isComplete(PermissionState.READY))
        assertEquals(SetupAction.NONE, SetupWizard.actionFor(SetupStep.DONE))
    }

    @Test fun `incomplete states are not complete`() {
        listOf(
            PermissionState.SHIZUKU_NOT_INSTALLED, PermissionState.SHIZUKU_NOT_RUNNING,
            PermissionState.PERMISSION_REQUIRED, PermissionState.PERMISSION_DENIED,
            PermissionState.SERVICE_DISCONNECTED, PermissionState.UNSUPPORTED,
        ).forEach { assertFalse(it.name, SetupWizard.isComplete(it)) }
    }

    @Test fun `each numbered step has exactly one primary action`() {
        assertEquals(SetupAction.GET_SHIZUKU, SetupWizard.actionFor(SetupStep.INSTALL))
        assertEquals(SetupAction.OPEN_SHIZUKU, SetupWizard.actionFor(SetupStep.START))
        assertEquals(SetupAction.REQUEST_PERMISSION, SetupWizard.actionFor(SetupStep.GRANT))
        assertEquals(SetupAction.RECONNECT, SetupWizard.actionFor(SetupStep.RECONNECT))
        assertEquals(SetupAction.NONE, SetupWizard.actionFor(SetupStep.UNSUPPORTED))
    }

    @Test fun `step numbers fit the total`() {
        val numbered = SetupStep.entries.mapNotNull { it.number }
        assertEquals((1..SetupWizard.TOTAL_STEPS).toList(), numbered)
    }
}
