package com.jev.probe.capture

internal enum class OverlayRecovery {
    ENABLE_ACCESSIBILITY, RECONNECT_ACCESSIBILITY, GRANT_OVERLAY, RESTORE;

    companion object {
        fun action(accessibilityEnabled: Boolean, connected: Boolean, overlayGranted: Boolean): OverlayRecovery = when {
            !accessibilityEnabled -> ENABLE_ACCESSIBILITY
            !connected -> RECONNECT_ACCESSIBILITY
            !overlayGranted -> GRANT_OVERLAY
            else -> RESTORE
        }
    }
}
