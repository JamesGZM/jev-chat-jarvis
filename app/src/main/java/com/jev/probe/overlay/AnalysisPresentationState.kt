package com.jev.probe.overlay

import com.jev.probe.core.Analysis
import com.jev.probe.core.RankedReply
import com.jev.probe.core.ReplyAdvice

/** Judgment and reply requests complete independently, in either order. */
internal class AnalysisPresentationState {
    private var judgment: Analysis? = null
    private var replies: List<RankedReply>? = null
    private var replyAdvice: ReplyAdvice? = null
    var replyError: String? = null
        private set

    val analysis: Analysis? get() = judgment?.copy(rankedReplies = replies ?: emptyList(), replyAdvice = replyAdvice)
    val generating: Boolean get() = replies == null

    fun receiveJudgment(value: Analysis) { judgment = value }
    fun receiveReplies(value: List<RankedReply>, error: String?, advice: ReplyAdvice? = null) {
        replies = value.toList()
        replyError = error
        replyAdvice = if (error == null) advice else null
    }
    fun reset() { judgment = null; replies = null; replyError = null; replyAdvice = null }
}
