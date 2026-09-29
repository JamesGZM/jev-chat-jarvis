package com.jev.probe.capture

internal object UnknownConversationReset {
    fun shouldReset(knownConversation: Boolean, chatAppEvent: Boolean, pageChanged: Boolean): Boolean =
        !knownConversation && chatAppEvent && pageChanged
}
