package com.jev.probe.overlay

import com.jev.probe.core.Analysis
import com.jev.probe.core.RankedReply
import org.junit.Assert.*
import org.junit.Test

class AnalysisPresentationStateTest {
    private val judgment = Analysis(null, null, null, null, null, null, null, emptyList(), 10)
    private val replies = listOf(RankedReply("Example reply", 0.8))

    @Test fun repliesBeforeJudgmentAreRetained() {
        val state = AnalysisPresentationState()
        state.receiveReplies(replies, null)
        assertNull(state.analysis)
        state.receiveJudgment(judgment)
        assertEquals(replies, state.analysis!!.rankedReplies)
        assertFalse(state.generating)
    }

    @Test fun judgmentBeforeRepliesStopsLoadingOnCompletion() {
        val state = AnalysisPresentationState()
        state.receiveJudgment(judgment)
        assertTrue(state.generating)
        state.receiveReplies(replies, null)
        assertEquals(replies, state.analysis!!.rankedReplies)
        assertFalse(state.generating)
    }

    @Test fun earlyReplyErrorSurvivesLaterJudgment() {
        val state = AnalysisPresentationState()
        state.receiveReplies(emptyList(), "timeout")
        state.receiveJudgment(judgment)
        assertFalse(state.generating)
        assertEquals("timeout", state.replyError)
    }

    @Test fun newAnalysisCannotReusePreviousRepliesOrError() {
        val state = AnalysisPresentationState()
        state.receiveJudgment(judgment)
        state.receiveReplies(replies, "old error")
        state.reset()
        assertNull(state.analysis)
        assertNull(state.replyError)
        assertTrue(state.generating)
        state.receiveJudgment(judgment)
        assertTrue(state.analysis!!.rankedReplies.isEmpty())
    }
}
