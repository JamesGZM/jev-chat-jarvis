package com.jev.probe.jev

import com.jev.probe.core.RankedReply
import org.json.JSONObject

internal object ReplyRanking {
    fun parse(answers: JSONObject, candidates: List<String>): List<RankedReply> {
        val probs = answers.optJSONObject("best_reply")?.optJSONObject("probabilities")
        val keys = listOf("reply_a", "reply_b", "reply_c")
        val scores = candidates.indices.map { i ->
            (probs?.opt(keys[i]) as? Number)?.toDouble()
        }
        // An incomplete ranking is not a zero-percent judgment on the missing options.
        if (scores.any { it == null || !it.isFinite() || it !in 0.0..1.0 }) {
            return candidates.map { RankedReply(it, Double.NaN) }
        }
        return candidates.mapIndexed { i, text -> RankedReply(text, scores[i]!!) }
            .sortedByDescending { it.prob }
    }
}
