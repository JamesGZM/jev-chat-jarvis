package com.jev.probe.jev

import com.jev.probe.core.ChatSnapshot
import com.jev.probe.core.Msg
import org.junit.Assert.*
import org.junit.Test

class ReplyPromptTest {
    @Test fun missingCaptureContextIsPassedAlongWithTheDecisionRequest() {
        val prompt = ReplyPrompt.user(ChatSnapshot(null, listOf(Msg("other", "消息")),
            note = "无法区分左右"), "朋友", "")
        assertTrue(prompt.contains("采集限制：无法区分左右"))
        assertTrue(prompt.contains("先选择行动"))
        assertTrue(ReplyPrompt.system.contains("默认 wait"))
        assertTrue(ReplyPrompt.system.contains("后两者 replies 必须为空数组"))
    }

    @Test fun intentionalWordplayIsPreservedWithoutAddingATypoExplanation() {
        val prompt = ReplyPrompt.user(ChatSnapshot(null, listOf(Msg("me", "蛋丁"))), "朋友", "")
        assertTrue(prompt.contains("[me / 我已发送]：蛋丁"))
        assertFalse(prompt.contains("但丁"))
        assertFalse(prompt.contains("手滑"))
        assertTrue(ReplyPrompt.system.contains("除非我在记录中明确承认笔误或要求纠正"))
        assertTrue(ReplyPrompt.system.contains("禁止编造手滑、打错字、输入法或自动纠正等原因"))
    }

    @Test fun outgoingTailRemainsMyFollowUpInsteadOfTheirReply() {
        val prompt = ReplyPrompt.user(ChatSnapshot(null, listOf(
            Msg("other", "进度如何？"), Msg("me", "正在测试"), Msg("me", "下午给你结果")
        )), "同事", "")
        assertTrue(prompt.contains("[other / 对方已发送]：进度如何？"))
        assertTrue(prompt.contains("[me / 我已发送]：下午给你结果"))
        assertTrue(prompt.contains("最后发言人：me（我）。对方尚未回复。"))
        assertFalse(prompt.contains("最后发言人：other"))
    }

    @Test fun incomingTailRequestsMyReply() {
        val prompt = ReplyPrompt.user(ChatSnapshot(null, listOf(
            Msg("me", "明天方便吗？"), Msg("other", "下午可以")
        )), "朋友", "")
        assertTrue(prompt.contains("最后发言人：other（对方）"))
        assertTrue(prompt.contains("由 me 发给 other"))
    }

    @Test fun historyCannotDetermineCurrentSpeaker() {
        val prompt = ReplyPrompt.user(ChatSnapshot(null, listOf(Msg("me", "收到"))),
            "老板", "更早的聊天记录：\n对方：完成后告诉我\n")
        assertTrue(prompt.contains("关系：老板"))
        assertTrue(prompt.contains("对方：完成后告诉我"))
        assertTrue(prompt.contains("最后发言人：me（我）"))
    }

    @Test fun currentWindowKeepsOrderAndEmptyWindowDoesNotInventSpeaker() {
        val messages = (0..10).map { Msg(if (it % 2 == 0) "me" else "other", "消息$it!") }
        val prompt = ReplyPrompt.user(ChatSnapshot(null, messages), "朋友", "")
        assertFalse(prompt.contains("消息0!"))
        assertTrue(prompt.indexOf("消息1!") < prompt.indexOf("消息10!"))
        val empty = ReplyPrompt.user(ChatSnapshot(null, emptyList()), "朋友", "")
        assertTrue(empty.contains("最后发言人：未知"))
    }
}
