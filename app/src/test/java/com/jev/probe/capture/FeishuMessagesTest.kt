package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class FeishuMessagesTest {
    private fun node(name: String = "", text: String = "", desc: String = "", longClick: Boolean = false,
                     children: List<FeishuNode> = emptyList(), left: Int = 0, right: Int = 1440) =
        FeishuNode(if (name.isEmpty()) "" else "com.ss.android.lark:id/$name", text, desc, longClick,
            left = left, right = right, children = children)

    @Test fun assistantBodyIsReadButTopicSuggestionsAndInputAreExcluded() {
        val body = node("content_view", longClick = true, children = listOf(
            node(text = "实际回复", desc = "实际回复", longClick = true)))
        val suggestions = node("content_view", children = listOf(
            node(text = "帮我翻译文档"), node(text = "帮我约会议")))
        val root = node(children = listOf(node("message_list", children = listOf(
            body, node("content_view", children = listOf(node("topic_text", "聊聊新话题"))), suggestions)),
            node("input_text", "未发送草稿")))
        val messages = feishuMessages(root)
        assertEquals(listOf("实际回复"), messages.map { it.text })
        assertEquals("other", messages.single().side)
    }

    @Test fun longAssistantMessageDoesNotBecomeMineByCrossingScreenCenter() {
        val body = node("content_view", left = 44, right = 1396, longClick = true,
            children = listOf(node(text = "长回复", desc = "长回复", longClick = true)))
        val messages = feishuMessages(node("message_list", children = listOf(body)))
        assertEquals("other", messages.single().side)
    }

    @Test fun legacyReadStateIsFoundBesideBubbleAndInputIsNeverAMessage() {
        val bubble = node("bubble_content_container", children = listOf(node("message")))
        val root = node(children = listOf(node("chat_message_list_view", children = listOf(
            node("msg_swipe_view", children = listOf(bubble, node("time_read_state_container_align_bubble"))),
            node("msg_swipe_view", children = listOf(bubble)))), node("kb_rich_text_content", "草稿")))
        val messages = feishuMessages(root)
        assertEquals(listOf("me", "other"), messages.map { it.side })
        assertTrue(messages.all { it.text == null })
    }

    @Test fun legacyReadableTextOnlyComesFromMessageBody() {
        val body = node("message", children = listOf(node(text = "正文")))
        val bubble = node("bubble_content_container", children = listOf(body))
        val row = node("msg_swipe_view", children = listOf(node("name_tv", "姓名"), bubble, node("date_tv", "昨天")))
        val root = node("chat_message_list_view", children = listOf(row))
        assertEquals(listOf("正文"), feishuMessages(root).map { it.text })
    }

    @Test fun manualAndAutomaticFeishuOcrCannotFallBackToWholeScreen() {
        assertEquals(OcrScope.BUBBLES, ocrScope("com.ss.android.lark", true))
        assertEquals(OcrScope.UNAVAILABLE, ocrScope("com.ss.android.lark", false))
        assertEquals(OcrScope.WHOLE_SCREEN, ocrScope("other.app", false))
    }
}
