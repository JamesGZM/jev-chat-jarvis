package com.jev.probe.core

enum class ReplyAdvice(val message: String) {
    REPLY("可以回复"),
    WAIT("暂时无需补充，可以等对方回应。"),
    INSUFFICIENT_CONTEXT("当前上下文不足，暂不生成候选，避免猜测。")
}

data class ReplySuggestion(val advice: ReplyAdvice, val replies: List<RankedReply>)
