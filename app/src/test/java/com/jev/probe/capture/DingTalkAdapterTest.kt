package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class DingTalkAdapterTest {
    private fun snapshot(rows: List<DingTalkRow>) = dingTalkSnapshot(true, true, " 同事 ", rows, 1440)!!

    @Test fun adapterIsRegistered() {
        assertTrue(createChatAppAdapters()["com.alibaba.android.rimet"] is DingTalkAdapter)
    }
    @Test fun longIncomingMessagesUseAvatarAndStayOneMessage() {
        val longText = "这是一个跨过屏幕中线的长消息，请把接入文档发我一下。"
        val result = snapshot(listOf(DingTalkRow(100, 104, longText, longText)))
        assertEquals("同事", result.title)
        assertEquals(1, result.messages.size)
        assertEquals("other", result.latestFrom)
        assertEquals(longText, result.messages.single().text)
    }
    @Test fun keepsSenderAndOrderIncludingImagePlaceholder() {
        val result = snapshot(listOf(
            DingTalkRow(300, 104, "文档发我一下", null),
            DingTalkRow(100, 1336, "哪个模型？", null),
            DingTalkRow(200, 104, null, "[图片]")
        ))
        assertEquals(listOf("me", "other", "other"), result.messages.map { it.side })
        assertEquals("[图片]", result.messages[1].text)
        assertEquals("文档发我一下", result.messages.last().text)
    }
    @Test fun cannotGuessSenderOfSystemRows() {
        assertTrue(snapshot(listOf(DingTalkRow(100, null, "9月29日 09:00", null))).messages.isEmpty())
    }
    @Test fun repeatedMessagesAreNotDeduplicatedByText() {
        assertEquals(2, snapshot(listOf(
            DingTalkRow(100, 104, "好的", null), DingTalkRow(200, 1336, "好的", null)
        )).messages.size)
    }
    @Test fun listPageAndMissingInputAreNotAConversation() {
        assertNull(dingTalkSnapshot(true, false, "消息", emptyList(), 1440))
        assertNull(dingTalkSnapshot(false, true, "搜索", emptyList(), 1440))
    }
}
