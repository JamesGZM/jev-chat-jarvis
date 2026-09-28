package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class ChatAppAdaptersTest {
    @Test fun wechatIsRoutedToItsDedicatedAdapter() {
        assertTrue(createChatAppAdapters()["com.tencent.mm"] is WeChatAdapter)
    }

    @Test fun existingAppsKeepTheirDedicatedAdapters() {
        val adapters = createChatAppAdapters()
        assertTrue(adapters["com.tencent.mobileqq"] is QQAdapter)
        assertTrue(adapters["com.twitter.android"] is XAdapter)
        assertTrue(adapters["com.ss.android.lark"] is FeishuAdapter)
        assertNull(adapters["com.example.unadapted"])
    }
}
