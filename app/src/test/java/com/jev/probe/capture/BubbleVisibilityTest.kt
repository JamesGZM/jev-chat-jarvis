package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class BubbleVisibilityTest {
    private val policy = BubbleVisibility("com.jev.probe")

    @Test fun chatListsAndUnadaptedAppsKeepAnEntryPoint() {
        for (pkg in listOf("com.tencent.mm", "com.tencent.mobileqq", "com.ss.android.lark", "org.telegram.messenger")) {
            assertEquals(BubbleVisibility.Decision.SHOW, policy.decide(true, pkg))
        }
    }

    @Test fun transientMissingWindowDoesNotHideBubble() {
        policy.decide(true, "com.tencent.mm")
        assertEquals(BubbleVisibility.Decision.KEEP, policy.decide(true, null))
        assertEquals(BubbleVisibility.Decision.SHOW, policy.decide(true, "com.tencent.mm"))
    }

    @Test fun manualHideSurvivesContentEventsAndMissingRoot() {
        policy.hideForVisit("com.tencent.mm")
        repeat(3) { assertEquals(BubbleVisibility.Decision.HIDE, policy.decide(true, "com.tencent.mm")) }
        policy.decide(true, null)
        assertEquals(BubbleVisibility.Decision.HIDE, policy.decide(true, "com.tencent.mm"))
    }

    @Test fun leavingAppOrExplicitRestoreEndsManualHide() {
        policy.hideForVisit("com.tencent.mm")
        policy.decide(true, "com.android.launcher")
        assertEquals(BubbleVisibility.Decision.SHOW, policy.decide(true, "com.tencent.mm"))
        policy.hideForVisit("com.tencent.mm")
        policy.restore()
        assertEquals(BubbleVisibility.Decision.SHOW, policy.decide(true, "com.tencent.mm"))
    }

    @Test fun pauseHomeAndOwnSettingsRemainHidden() {
        assertEquals(BubbleVisibility.Decision.HIDE, policy.decide(false, "com.tencent.mm"))
        for (pkg in listOf("com.jev.probe", "com.android.systemui", "com.miui.home", "com.huawei.android.launcher")) {
            assertEquals(BubbleVisibility.Decision.HIDE, policy.decide(true, pkg))
        }
    }
}
