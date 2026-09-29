package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class QQTitleTest {
    @Test fun currentPrivateChatTitleIsReadAndTrimmed() {
        assertEquals("Alice", qqConversationTitle("com.tencent.mobileqq:id/3kc", " Alice ", true))
    }

    @Test fun currentPrivateChatTitleMustBeVisibleAndNonempty() {
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/3kc", "Alice", false))
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/3kc", " ", true))
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/3kc", null, true))
    }

    @Test fun privateChatTitleIsReadWithoutRequiringCenteredPosition() {
        assertEquals("Alice", qqConversationTitle("com.tencent.mobileqq:id/3g3", "Alice", true))
    }

    @Test fun groupChatTitleStillWorks() {
        assertEquals("Friends (3)", qqConversationTitle("com.tencent.mobileqq:id/371", "Friends (3)", true))
    }

    @Test fun onlineStatusAndMessageTextCannotBecomeTitle() {
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/j64", "在线", true))
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/mjn", "hello", true))
    }

    @Test fun HiddenOrEmptyTitleIsRejected() {
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/3g3", "Alice", false))
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/3g3", " ", true))
        assertNull(qqConversationTitle("com.tencent.mobileqq:id/3g3", null, true))
    }
}
