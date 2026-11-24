package com.graydial.android.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStateResolverTest {

    private val ready = ShizukuSnapshot(
        installed = true, binderAlive = true, preV11 = false, permissionGranted = true,
        permanentlyDenied = false, deniedThisSession = false, service = ServiceState.CONNECTED,
    )

    private fun resolve(s: ShizukuSnapshot) = PermissionStateResolver.resolve(s)

    @Test fun ready() = assertEquals(PermissionState.READY, resolve(ready))

    @Test fun `not installed`() = assertEquals(
        PermissionState.SHIZUKU_NOT_INSTALLED,
        resolve(ready.copy(installed = false, binderAlive = false)),
    )

    @Test fun `installed but not running`() = assertEquals(
        PermissionState.SHIZUKU_NOT_RUNNING,
        resolve(ready.copy(binderAlive = false)),
    )

    @Test fun `binder alive without visible package (e g Sui) is not reported as missing`() = assertEquals(
        PermissionState.READY,
        resolve(ready.copy(installed = false)),
    )

    @Test fun `permission required`() = assertEquals(
        PermissionState.PERMISSION_REQUIRED,
        resolve(ready.copy(permissionGranted = false, service = ServiceState.NOT_BOUND)),
    )

    @Test fun `permission denied this session`() = assertEquals(
        PermissionState.PERMISSION_DENIED,
        resolve(ready.copy(permissionGranted = false, deniedThisSession = true, service = ServiceState.NOT_BOUND)),
    )

    @Test fun `permission permanently denied`() = assertEquals(
        PermissionState.PERMISSION_DENIED,
        resolve(ready.copy(permissionGranted = false, permanentlyDenied = true, service = ServiceState.NOT_BOUND)),
    )

    @Test fun `granted but not yet bound`() = assertEquals(
        PermissionState.PERMISSION_GRANTED,
        resolve(ready.copy(service = ServiceState.NOT_BOUND)),
    )

    @Test fun `service disconnected`() = assertEquals(
        PermissionState.SERVICE_DISCONNECTED,
        resolve(ready.copy(service = ServiceState.DISCONNECTED)),
    )

    @Test fun `unsupported operation`() = assertEquals(
        PermissionState.UNSUPPORTED,
        resolve(ready.copy(service = ServiceState.UNSUPPORTED)),
    )

    @Test fun `pre v11 Shizuku is unsupported`() = assertEquals(
        PermissionState.UNSUPPORTED,
        resolve(ready.copy(preV11 = true)),
    )

    @Test fun `revocation while connected falls back to permission required`() = assertEquals(
        PermissionState.PERMISSION_REQUIRED,
        resolve(ready.copy(permissionGranted = false)),
    )

    @Test fun `operations allowed only in usable states`() {
        assertTrue(PermissionState.READY.canAttemptOperation)
        assertTrue(PermissionState.SERVICE_DISCONNECTED.canAttemptOperation)
        assertFalse(PermissionState.SHIZUKU_NOT_RUNNING.canAttemptOperation)
        assertFalse(PermissionState.PERMISSION_DENIED.canAttemptOperation)
    }
}
