package com.jev.probe.jev

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ReplyReviewTest {
    @Test fun binaryDecisionDoesNotRequireAnUncalibratedHighConfidence() {
        val candidates = listOf("被判不合格", "无法确定", "被判合格")
        val answers = JSONObject("""{"valid_reply_0":{"noul":0.49},"valid_reply_1":{"noul":0.5},"valid_reply_2":{"noul":0.65}}""")
        assertEquals(listOf("被判合格"), ReplyReview.accepted(answers, candidates).map { it.text })
    }

    @Test fun invalidWinnerIsRemovedWithoutChangingCandidateIdentity() {
        val answers = JSONObject("""{
            "valid_reply_0":{"noul":0.1},"valid_reply_1":{"noul":0.95},"valid_reply_2":{"noul":0.9},
            "best_reply":{"probabilities":{"reply_a":0.8,"reply_b":0.05,"reply_c":0.15}}
        }""")
        val result = ReplyReview.accepted(answers, listOf("替对方说话", "自然承接", "简短玩笑"))
        assertEquals(listOf("简短玩笑", "自然承接"), result.map { it.text })
        assertEquals(0.15, result.first().prob, 0.001)
    }

    @Test fun aSingleCandidateRequiresReviewButNoRankingQuestion() {
        val questions = JevQuestions.replyReview(listOf("候选"))
        assertTrue(questions.has("valid_reply_0"))
        assertFalse(questions.has("best_reply"))
        assertTrue(ReplyReview.accepted(JSONObject("""{"valid_reply_0":{"noul":0.2}}"""), listOf("候选")).isEmpty())
        assertEquals(1, ReplyReview.accepted(JSONObject("""{"valid_reply_0":{"noul":0.9}}"""), listOf("候选")).size)
    }

    @Test fun reviewQuestionsCoverEveryCandidateAlongsideRanking() {
        val questions = JevQuestions.replyReview(listOf("一", "二", "三"))
        assertEquals(4, questions.length())
        for (i in 0..2) assertEquals("noul", questions.getJSONObject("valid_reply_$i").getString("type"))
        assertTrue(questions.has("best_reply"))
    }

    @Test fun missingAndMalformedChecksFailInsteadOfLettingCandidatesThrough() {
        for (json in listOf("{}", """{"valid_reply_0":{"noul":null}}""",
            """{"valid_reply_0":{"noul":2}}""", """{"valid_reply_0":{"noul":"yes"}}""")) {
            assertTrue(runCatching { ReplyReview.accepted(JSONObject(json), listOf("私密候选")) }.exceptionOrNull() is IllegalArgumentException)
        }
    }
}
