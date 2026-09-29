package com.jev.probe.capture

/** A small immutable node tree keeps message selection testable without Android. */
internal data class FeishuNode(
    val id: String = "",
    val text: String = "",
    val description: String = "",
    val longClickable: Boolean = false,
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val children: List<FeishuNode> = emptyList()
) {
    fun all(): Sequence<FeishuNode> = sequence {
        yield(this@FeishuNode)
        children.forEach { yieldAll(it.all()) }
    }
    fun named(name: String) = id == "com.ss.android.lark:id/$name"
}

internal data class FeishuMessage(val node: FeishuNode, val side: String, val text: String?)

internal fun feishuMessages(root: FeishuNode): List<FeishuMessage> {
    val nodes = root.all().toList()
    val legacy = nodes.firstOrNull { it.named("chat_message_list_view") }
    if (legacy != null) {
        return legacy.all().filter { it.named("msg_swipe_view") }.flatMap { row ->
            val side = if (row.all().any { it.named("time_read_state_container_align_bubble") }) "me" else "other"
            row.all().filter { it.named("bubble_content_container") }.map { bubble ->
                val body = bubble.all().filter { it.named("message") }.flatMap { it.all() }
                    .map { it.text.trim() }.filter { it.isNotEmpty() }.toList()
                FeishuMessage(bubble, side, body.joinToString("\n").takeIf { it.isNotBlank() })
            }
        }.toList().sortedBy { it.node.top }
    }
    val list = nodes.firstOrNull { it.named("message_list") } ?: return emptyList()
    // The observed assistant body supports long-press and exposes the same text
    // as its accessibility description. Topic labels and suggestion buttons don't.
    return list.all().filter { it.named("content_view") && it.longClickable }.mapNotNull { row ->
        val body = row.all().filter {
            it.longClickable && it.text.isNotBlank() && it.description == it.text
        }.map { it.text.trim() }.toList()
        if (body.isEmpty()) null else {
            // Assistant responses span the list width; outgoing bubbles have a
            // larger left inset. Compare margins, not text/bubble midpoint.
            val side = if (row.left - list.left > list.right - row.right) "me" else "other"
            FeishuMessage(row, side, body.joinToString("\n"))
        }
    }.toList().sortedBy { it.node.top }
}

internal enum class OcrScope { BUBBLES, WHOLE_SCREEN, UNAVAILABLE }
internal fun ocrScope(pkg: String, hasBubbles: Boolean): OcrScope = when {
    hasBubbles -> OcrScope.BUBBLES
    pkg == "com.ss.android.lark" -> OcrScope.UNAVAILABLE
    else -> OcrScope.WHOLE_SCREEN
}
