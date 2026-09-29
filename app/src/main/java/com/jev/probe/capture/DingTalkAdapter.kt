package com.jev.probe.capture

import android.content.res.Resources
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.jev.probe.core.ChatSnapshot
import com.jev.probe.core.Msg

internal data class DingTalkRow(
    val top: Int,
    val avatarCenter: Int?,
    val body: String?,
    val description: String?
)

internal fun dingTalkSnapshot(
    hasMessageList: Boolean,
    hasInputPanel: Boolean,
    title: String?,
    rows: List<DingTalkRow>,
    width: Int
): ChatSnapshot? {
    if (!hasMessageList || !hasInputPanel || width <= 0) return null
    val messages = rows.sortedBy { it.top }.mapNotNull { row ->
        val avatar = row.avatarCenter ?: return@mapNotNull null
        if (avatar !in 0..width || avatar == width / 2) return@mapNotNull null
        val text = row.body?.trim()?.takeIf { it.isNotEmpty() }
            ?: row.description?.trim()?.takeIf { it.isNotEmpty() }
            ?: return@mapNotNull null
        Msg(if (avatar > width / 2) "me" else "other", text)
    }
    return ChatSnapshot(title?.trim()?.takeIf { it.isNotEmpty() }, messages)
}

/** DingTalk exposes message text; whole-screen OCR would mix chrome and lose senders. */
class DingTalkAdapter : ChatAppAdapter {
    override val pkg = "com.alibaba.android.rimet"

    override fun extract(root: AccessibilityNodeInfo, res: Resources): ChatSnapshot? {
        fun find(node: AccessibilityNodeInfo, name: String) =
            node.findAccessibilityNodeInfosByViewId("$pkg:id/$name").filter { it.isVisibleToUser }

        val list = find(root, "list_view").firstOrNull() ?: return null
        val input = find(root, "rich_input_panel").isNotEmpty()
        if (!input) return null
        val title = find(root, "ll_title").firstOrNull()?.let { header ->
            find(header, "tv_title").firstOrNull()?.text?.toString()
        }
        val rows = find(list, "chatting_content_view_container").map { row ->
            val bounds = Rect().also { row.getBoundsInScreen(it) }
            // Long incoming bubbles cross the screen midpoint; the avatar does not.
            val avatar = find(row, "chattting_avatar").singleOrNull()?.let {
                val rect = Rect().also { b -> it.getBoundsInScreen(b) }
                if (rect.width() > 0 && rect.height() > 0) rect.centerX() else null
            }
            val body = find(row, "chatting_content_tv").mapNotNull { it.text?.toString() }
                .filter { it.isNotBlank() }.joinToString("\n").takeIf { it.isNotBlank() }
            val description = find(row, "chatting_content_view_stub").firstOrNull()
                ?.contentDescription?.toString()
            DingTalkRow(bounds.top, avatar, body, description)
        }
        return dingTalkSnapshot(true, input, title, rows, res.displayMetrics.widthPixels)
    }
}
