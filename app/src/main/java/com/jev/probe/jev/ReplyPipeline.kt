package com.jev.probe.jev

import com.jev.probe.core.ChatSnapshot
import com.jev.probe.core.RankedReply
import com.jev.probe.core.ReplyAdvice
import com.jev.probe.core.ReplySuggestion

/** Generate once; ranking failures must not discard usable drafts or trigger generation again. */
internal fun replySuggestion(
    snapshot: ChatSnapshot,
    draft: () -> ReplyDraft,
    rank: (List<String>) -> List<RankedReply>
): ReplySuggestion {
    val generated = try {
        draft()
    } catch (e: Exception) {
        if (e is InterruptedException || Thread.currentThread().isInterrupted) throw e
        if (snapshot.latestFrom != "other") throw e
        return incomingFallback()
    }
    if (generated.advice != ReplyAdvice.REPLY) {
        return if (snapshot.latestFrom == "other") incomingFallback()
        else ReplySuggestion(generated.advice, emptyList())
    }
    val ranked = try {
        rank(generated.candidates)
    } catch (e: Exception) {
        if (e is InterruptedException || Thread.currentThread().isInterrupted) throw e
        // No ranking score is available; keep the original candidate order.
        generated.candidates.map { RankedReply(it, Double.NaN) }
    }
    return ReplySuggestion(ReplyAdvice.REPLY, ranked)
}

private fun incomingFallback() = ReplySuggestion(
    ReplyAdvice.FALLBACK, listOf(RankedReply("我看到了。", Double.NaN))
)
