package com.jev.probe.jev

import com.jev.probe.core.ChatSnapshot
import com.jev.probe.core.RankedReply
import com.jev.probe.core.ReplyAdvice
import com.jev.probe.core.ReplySuggestion

/** An incoming message must always leave the user at least one usable option. */
internal fun replySuggestion(
    snapshot: ChatSnapshot,
    draft: (retry: Boolean) -> ReplyDraft,
    review: (List<String>) -> List<RankedReply>
): ReplySuggestion {
    val incoming = snapshot.latestFrom == "other"
    repeat(if (incoming) 2 else 1) { attempt ->
        try {
            val result = draft(attempt > 0).rankWith(review)
            if (!incoming || result.replies.isNotEmpty()) return result
        } catch (e: Exception) {
            if (e is InterruptedException || Thread.currentThread().isInterrupted) throw e
            if (!incoming) throw e
        }
    }
    // Acknowledge receipt only: no guessed facts, location, emotion or commitments.
    return ReplySuggestion(ReplyAdvice.FALLBACK, listOf(RankedReply("我看到了。", 0.0)))
}
