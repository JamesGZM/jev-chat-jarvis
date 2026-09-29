package com.jev.probe.jev

import com.jev.probe.core.*
import org.junit.Assert.*
import org.junit.Test

class ReplyPipelineTest {
    private val incoming = ChatSnapshot(null, listOf(Msg("me", "机器卡住了"), Msg("other", "哎呀")))
    private val candidates = listOf("又卡住了", "真会给我找事", "这机器又闹脾气了")

    @Test fun threeCandidatesUseExactlyOneGenerationAndOneRanking() {
        var drafts = 0
        var ranks = 0
        val result = replySuggestion(incoming,
            { drafts++; ReplyDraft(ReplyAdvice.REPLY, candidates) },
            { ranks++; it.reversed().map { text -> RankedReply(text, 0.1) } })
        assertEquals(1, drafts)
        assertEquals(1, ranks)
        assertEquals(candidates.reversed(), result.replies.map { it.text })
    }

    @Test fun rankingFailureKeepsAllDraftsWithoutRegenerationOrFakeScores() {
        var drafts = 0
        var ranks = 0
        val result = replySuggestion(incoming,
            { drafts++; ReplyDraft(ReplyAdvice.REPLY, candidates) },
            { ranks++; throw IllegalStateException("offline") })
        assertEquals(1, drafts)
        assertEquals(1, ranks)
        assertEquals(ReplyAdvice.REPLY, result.advice)
        assertEquals(candidates, result.replies.map { it.text })
        assertTrue(result.replies.all { it.prob.isNaN() })
    }

    @Test fun providerFailureDoesNotRetryAndStillOffersIncomingAcknowledgment() {
        var calls = 0
        val result = replySuggestion(incoming, { calls++; throw IllegalStateException("failed") }, { error("not called") })
        assertEquals(1, calls)
        assertEquals(ReplyAdvice.FALLBACK, result.advice)
        assertEquals("我看到了。", result.replies.single().text)
    }

    @Test fun incomingWaitDoesNotTriggerAnotherGeneration() {
        var calls = 0
        val result = replySuggestion(incoming, { calls++; ReplyDraft(ReplyAdvice.WAIT, emptyList()) }, { error("not called") })
        assertEquals(1, calls)
        assertEquals(ReplyAdvice.FALLBACK, result.advice)
    }

    @Test fun outgoingTailStillAllowsWaiting() {
        val result = replySuggestion(ChatSnapshot(null, listOf(Msg("me", "好的"))),
            { ReplyDraft(ReplyAdvice.WAIT, emptyList()) }, { error("not called") })
        assertEquals(ReplyAdvice.WAIT, result.advice)
        assertTrue(result.replies.isEmpty())
    }

    @Test(expected = InterruptedException::class) fun generationCancellationPropagates() {
        replySuggestion(incoming, { throw InterruptedException() }, { emptyList() })
    }

    @Test(expected = InterruptedException::class) fun rankingCancellationPropagates() {
        replySuggestion(incoming, { ReplyDraft(ReplyAdvice.REPLY, candidates) }, { throw InterruptedException() })
    }
}
