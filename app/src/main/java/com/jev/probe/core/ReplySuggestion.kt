package com.jev.probe.core

enum class ReplyAdvice(val message: String) {
    REPLY("可以回复"),
    WAIT("这段对话可以暂时停在这里，不必勉强接话。"),
    FILTERED("这次候选未通过视角与事实检查，已隐藏。可以重新生成。"),
    FALLBACK("暂未生成可靠的个性化回复，以下是通用确认，可编辑后再发送。"),
    INSUFFICIENT_CONTEXT("当前上下文不足，暂不生成候选，避免猜测。")
}

data class ReplySuggestion(val advice: ReplyAdvice, val replies: List<RankedReply>)
