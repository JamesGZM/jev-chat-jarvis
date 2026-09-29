package com.jev.probe.capture

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayRecoveryTest {
    @Test fun enabledPermissionWithDeadServiceRequiresReconnect() {
        assertEquals(OverlayRecovery.RECONNECT_ACCESSIBILITY, OverlayRecovery.action(true, false, true))
    }
    @Test fun healthyServiceCanRestoreWithoutChangingPermissions() {
        assertEquals(OverlayRecovery.RESTORE, OverlayRecovery.action(true, true, true))
    }
    @Test fun missingPermissionsHaveSpecificRecoveryPaths() {
        assertEquals(OverlayRecovery.ENABLE_ACCESSIBILITY, OverlayRecovery.action(false, false, true))
        assertEquals(OverlayRecovery.GRANT_OVERLAY, OverlayRecovery.action(true, true, false))
    }
    @Test fun reconnectChangesRecoveryPathToRestore() {
        assertEquals(OverlayRecovery.RECONNECT_ACCESSIBILITY, OverlayRecovery.action(true, false, true))
        assertEquals(OverlayRecovery.RESTORE, OverlayRecovery.action(true, true, true))
    }
}
