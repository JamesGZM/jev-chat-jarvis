package com.jev.probe.jev

import com.jev.probe.core.*
import org.junit.Assert.*
import org.junit.Test

class ReplyPipelineTest {
    private val incoming = ChatSnapshot(null, listOf(Msg("me", "机器卡住了"), Msg("other", "哎呀")))

    @Test fun incomingWaitIsRegeneratedInsteadOfDisplayedEmpty() {
        val attempts = mutableListOf<Boolean>()
        val result = replySuggestion(incoming, { retry ->
            attempts.add(retry)
            if (retry) ReplyDraft(ReplyAdvice.REPLY, listOf("真会给我找事")) else ReplyDraft(ReplyAdvice.WAIT, emptyList())
        }, { it.map { text -> RankedReply(text, 0.0) } })
        assertEquals(listOf(false, true), attempts)
        assertEquals("真会给我找事", result.replies.single().text)
    }

    @Test fun rejectedCandidatesStillLeaveAnIncomingReplyOption() {
        var calls = 0
        val result = replySuggestion(incoming, { calls++; ReplyDraft(ReplyAdvice.REPLY, listOf("不合格")) }, { emptyList() })
        assertEquals(2, calls)
        assertEquals(ReplyAdvice.FALLBACK, result.advice)
        assertEquals("我看到了。", result.replies.single().text)
    }

    @Test fun providerFailureCannotLeaveIncomingCandidatesEmpty() {
        val result = replySuggestion(incoming, { throw IllegalStateException("failed") }, { error("not called") })
        assertEquals(ReplyAdvice.FALLBACK, result.advice)
        assertEquals(1, result.replies.size)
    }

    @Test fun outgoingTailStillAllowsWaiting() {
        val result = replySuggestion(ChatSnapshot(null, listOf(Msg("me", "好的"))),
            { ReplyDraft(ReplyAdvice.WAIT, emptyList()) }, { error("not called") })
        assertEquals(ReplyAdvice.WAIT, result.advice)
        assertTrue(result.replies.isEmpty())
    }

    @Test fun successfulFirstAttemptIsNotRepeated() {
        var calls = 0
        val result = replySuggestion(incoming,
            { calls++; ReplyDraft(ReplyAdvice.REPLY, listOf("唉")) },
            { it.map { text -> RankedReply(text, 0.0) } })
        assertEquals(1, calls)
        assertEquals(1, result.replies.size)
    }

    @Test(expected = InterruptedException::class) fun cancellationDoesNotGenerateAFallbackForAnOldSession() {
        replySuggestion(incoming, { throw InterruptedException() }, { emptyList() })
    }
}
