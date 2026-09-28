package com.jev.probe.jev

import com.jev.probe.core.RankedReply
import org.json.JSONObject

internal object ReplyReview {
    // A ranking is relative; require a separate positive validity judgment first.
    private const val MIN_VALIDITY = 0.5

    fun accepted(answers: JSONObject, candidates: List<String>): List<RankedReply> {
        val validity = candidates.indices.map { i ->
            val value = answers.optJSONObject("valid_reply_$i")?.opt("noul")
            val score = (value as? Number)?.toDouble()
            require(score != null && score.isFinite() && score in 0.0..1.0) {
                "候选检查结果不完整，请重新分析"
            }
            score
        }
        val probs = answers.optJSONObject("best_reply")?.optJSONObject("probabilities")
        val keys = listOf("reply_a", "reply_b", "reply_c")
        return candidates.mapIndexedNotNull { i, text ->
            if (validity[i] <= MIN_VALIDITY) null
            else {
                val probability = probs?.optDouble(keys[i], 0.0) ?: 0.0
                RankedReply(text, if (probability.isFinite()) probability.coerceIn(0.0, 1.0) else 0.0)
            }
        }.sortedByDescending { it.prob }
    }
}
