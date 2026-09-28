package com.jev.probe.jev

import com.jev.probe.core.RankedReply
import com.jev.probe.core.ReplyAdvice
import org.junit.Assert.*
import org.junit.Test

class ReplyDraftTest {
    @Test fun waitingIsASuccessWithoutCandidatesOrRanking() {
        val draft = ReplyDraftParser.parse("""{"action":"wait","replies":[]}""")
        val result = draft.rankWith { error("must not rank") }
        assertEquals(ReplyAdvice.WAIT, result.advice)
        assertTrue(result.replies.isEmpty())
    }

    @Test fun insufficientContextDoesNotInventAHoldingReply() {
        val draft = ReplyDraftParser.parse("""{"action":"insufficient_context","replies":[]}""")
        val result = draft.rankWith { error("must not rank") }
        assertEquals(ReplyAdvice.INSUFFICIENT_CONTEXT, result.advice)
        assertTrue(result.replies.isEmpty())
    }

    @Test fun oneCandidateIsReviewedWithoutPadding() {
        val draft = ReplyDraftParser.parse("""{"action":"reply","replies":[" 明天下午可以 "]}""")
        var calls = 0
        val result = draft.rankWith { calls++; it.map { text -> RankedReply(text, 0.0) } }
        assertEquals(1, calls)
        assertEquals(listOf("明天下午可以"), result.replies.map { it.text })
    }

    @Test fun rejectedCandidatesAreNotMisrepresentedAsWaiting() {
        val draft = ReplyDraft(ReplyAdvice.REPLY, listOf("不可靠的候选"))
        val result = draft.rankWith { emptyList() }
        assertEquals(ReplyAdvice.FILTERED, result.advice)
        assertTrue(result.replies.isEmpty())
    }

    @Test fun twoAndThreeCandidatesAreRankedWithoutPadding() {
        for (n in 2..3) {
            val raw = (1..n).joinToString(",") { "\"候选$it\"" }
            val draft = ReplyDraftParser.parse("""{"action":"reply","replies":[$raw]}""")
            var calls = 0
            val result = draft.rankWith {
                calls++
                assertEquals(n, it.size)
                it.reversed().map { text -> RankedReply(text, 0.5) }
            }
            assertEquals(1, calls)
            assertEquals("候选$n", result.replies.first().text)
            val criteria = JevQuestions.rankQuestion(draft.candidates).getJSONObject("best_reply").getJSONObject("criteria")
            assertEquals(n, criteria.length())
            assertEquals(n == 3, criteria.has("reply_c"))
        }
    }

    @Test fun blankAndDuplicateCandidatesAreNotDisplayed() {
        val draft = ReplyDraftParser.parse("""{"action":"reply","replies":[" 好的 ","好的",""]}""")
        assertEquals(listOf("好的"), draft.candidates)
    }

    @Test fun fencedJsonIsAccepted() {
        assertEquals(ReplyAdvice.WAIT, ReplyDraftParser.parse("```json\n{\"action\":\"wait\",\"replies\":[]}\n```").advice)
    }

    @Test fun malformedAndContradictoryResponsesAreErrorsNotWaitingOrFakeReplies() {
        val invalid = listOf("", "[]", "private response text", "{}",
            """{"action":"unknown","replies":[]}""",
            """{"action":"reply","replies":[]}""",
            """{"action":"reply","replies":[" "]}""",
            """{"action":"reply","replies":[null]}""",
            """{"action":"reply","replies":[123]}""",
            """{"action":"wait","replies":["fake"]}""",
            """{"action":"reply","replies":["1","2","3","4"]}""")
        invalid.forEach {
            val failure = runCatching { ReplyDraftParser.parse(it) }.exceptionOrNull()
            assertTrue(failure is IllegalArgumentException)
            assertEquals("回复接口返回格式异常，请重新分析", failure?.message)
        }
    }
}
