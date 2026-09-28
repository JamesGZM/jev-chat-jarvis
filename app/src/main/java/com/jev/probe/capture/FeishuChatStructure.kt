package com.jev.probe.capture

/** Resolve only known chat editors; ambiguous or hidden nodes must not receive text. */
internal fun <T> resolveFeishuInput(findById: (String) -> List<T>, canWrite: (T) -> Boolean): T? =
    listOf("com.ss.android.lark:id/kb_rich_text_content", "com.ss.android.lark:id/input_text")
        .flatMap(findById).distinct().filter(canWrite).singleOrNull()

/** Require the whole newer chat shell so a list/search input cannot trigger OCR. */
internal class FeishuChatStructure {
    private var legacyChat = false
    private var chatRoot = false
    private var messageList = false
    private var inputText = false

    fun observe(id: String) {
        when (id) {
            "com.ss.android.lark:id/message",
            "com.ss.android.lark:id/bubble_content_container",
            "com.ss.android.lark:id/kb_rich_text_content" -> legacyChat = true
            "com.ss.android.lark:id/chat_root" -> chatRoot = true
            "com.ss.android.lark:id/message_list" -> messageList = true
            "com.ss.android.lark:id/input_text" -> inputText = true
        }
    }

    val usesNewShell: Boolean get() = chatRoot && messageList && inputText
    val isChat: Boolean get() = legacyChat || usesNewShell
}
