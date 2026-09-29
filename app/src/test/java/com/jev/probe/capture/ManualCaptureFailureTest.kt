package com.jev.probe.capture

import org.junit.Assert.assertTrue
import org.junit.Test

class ManualCaptureFailureTest {
    @Test fun emptyTreeDoesNotTellUserTheyAreOutsideChat() {
        assertTrue(ManualCaptureFailure.message(true, true, false).contains("未向无障碍服务提供内容"))
    }
    @Test fun missingWindowHasServiceRecoveryAdvice() {
        assertTrue(ManualCaptureFailure.message(false, false, false).contains("读取服务"))
    }
    @Test fun chatWithoutTitleExplainsTitleFailure() {
        assertTrue(ManualCaptureFailure.message(true, false, true).contains("无法读取会话标题"))
    }
    @Test fun unrecognizedPageExplainsPossibleAdapterMismatch() {
        assertTrue(ManualCaptureFailure.message(true, false, false).contains("尚未适配"))
    }
}
