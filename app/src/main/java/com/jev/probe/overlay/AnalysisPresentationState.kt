package com.jev.probe.overlay

import com.jev.probe.core.Analysis
import com.jev.probe.core.RankedReply

/** Judgment and reply requests complete independently, in either order. */
internal class AnalysisPresentationState {
    private var judgment: Analysis? = null
    private var replies: List<RankedReply>? = null
    var replyError: String? = null
        private set

    val analysis: Analysis? get() = judgment?.copy(rankedReplies = replies ?: emptyList())
    val generating: Boolean get() = replies == null

    fun receiveJudgment(value: Analysis) { judgment = value }
    fun receiveReplies(value: List<RankedReply>, error: String?) {
        replies = value.toList()
        replyError = error
    }
    fun reset() { judgment = null; replies = null; replyError = null }
}
