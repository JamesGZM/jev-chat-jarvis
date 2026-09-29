package com.jev.probe.jev

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ReplyRankingTest {
    @Test fun rankingKeepsAllThreeEvenWithLowScoresAndOldValidityFields() {
        val answers = JSONObject("""{"valid_reply_0":{"noul":0.1},"best_reply":{"probabilities":{"reply_a":0.01,"reply_b":0.79,"reply_c":0.20}}}""")
        val ranked = ReplyRanking.parse(answers, listOf("一", "二", "三"))
        assertEquals(listOf("二", "三", "一"), ranked.map { it.text })
        assertEquals(listOf(0.79, 0.20, 0.01), ranked.map { it.prob })
    }

    @Test fun rankingRequestContainsOnlyOneChoiceQuestion() {
        val questions = JevQuestions.rankQuestion(listOf("一", "二", "三"))
        assertEquals(1, questions.length())
        assertEquals("choice", questions.getJSONObject("best_reply").getString("type"))
        assertEquals(3, questions.getJSONObject("best_reply").getJSONObject("criteria").length())
    }

    @Test fun missingOrInvalidScoresPreserveCandidatesWithoutFakePercentages() {
        for (raw in listOf("{}", """{"best_reply":{"probabilities":{"reply_a":0.7}}}""",
            """{"best_reply":{"probabilities":{"reply_a":2,"reply_b":0,"reply_c":0}}}""")) {
            val result = ReplyRanking.parse(JSONObject(raw), listOf("一", "二", "三"))
            assertEquals(listOf("一", "二", "三"), result.map { it.text })
            assertTrue(result.all { it.prob.isNaN() })
        }
    }
}
