package com.jev.probe.capture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnknownConversationResetTest {
    @Test fun switchingBetweenUnreadablePagesResetsPanel() {
        assertTrue(UnknownConversationReset.shouldReset(false, true, true))
    }
    @Test fun ordinaryContentRefreshPreservesFailureReason() {
        assertFalse(UnknownConversationReset.shouldReset(false, true, false))
    }
    @Test fun overlayEventsCannotEraseFailureReason() {
        assertFalse(UnknownConversationReset.shouldReset(false, false, true))
    }
    @Test fun recognizedConversationsUseExistingIdentityChecks() {
        assertFalse(UnknownConversationReset.shouldReset(true, true, true))
    }
}
