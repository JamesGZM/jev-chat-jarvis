package com.jev.probe.jev

import com.jev.probe.core.ReplyAdvice
import com.jev.probe.core.RankedReply
import com.jev.probe.core.ReplySuggestion
import org.json.JSONObject

data class ReplyDraft(val advice: ReplyAdvice, val candidates: List<String>)

internal fun ReplyDraft.rankWith(rank: (List<String>) -> List<RankedReply>): ReplySuggestion {
    val replies = when {
        advice != ReplyAdvice.REPLY -> emptyList()
        else -> rank(candidates)
    }
    val resultAdvice = if (advice == ReplyAdvice.REPLY && replies.isEmpty()) ReplyAdvice.FILTERED else advice
    return ReplySuggestion(resultAdvice, replies)
}

internal object ReplyDraftParser {
    fun parse(content: String): ReplyDraft {
        try {
            val json = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(json)
            val advice = when (obj.getString("action")) {
                "reply" -> ReplyAdvice.REPLY
                "wait" -> ReplyAdvice.WAIT
                "insufficient_context" -> ReplyAdvice.INSUFFICIENT_CONTEXT
                else -> error("unknown action")
            }
            val replies = obj.getJSONArray("replies")
            require(replies.length() <= 3)
            val candidates = (0 until replies.length()).map {
                val value = replies.get(it)
                require(value is String)
                value.trim()
            }.filter { it.isNotEmpty() }.distinct()
            if (advice == ReplyAdvice.REPLY) require(candidates.isNotEmpty())
            else require(replies.length() == 0)
            return ReplyDraft(advice, candidates)
        } catch (_: Exception) {
            // Do not surface raw model output (which may contain private chat text).
            throw IllegalArgumentException("回复接口返回格式异常，请重新分析")
        }
    }
}
