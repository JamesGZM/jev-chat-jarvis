package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class FeishuChatStructureTest {
    private data class Editor(val id: String, val writable: Boolean = true)
    private fun resolve(vararg editors: Editor) = resolveFeishuInput(
        findById = { id -> editors.filter { "com.ss.android.lark:id/${it.id}" == id } },
        canWrite = { it.writable }
    )

    @Test fun newChatEditorCanBeResolved() {
        val input = Editor("input_text")
        assertEquals(input, resolve(Editor("input"), input))
    }

    @Test fun legacyEditorStillWorks() {
        val input = Editor("kb_rich_text_content")
        assertEquals(input, resolve(input))
    }

    @Test fun hiddenOrDisabledLegacyEditorCannotMaskNewEditor() {
        val input = Editor("input_text")
        assertEquals(input, resolve(Editor("kb_rich_text_content", false), input))
        assertNull(resolve(Editor("input_text", false)))
    }

    @Test fun ambiguousEditorsAndUnrelatedInputsAreRejected() {
        assertNull(resolve(Editor("kb_rich_text_content"), Editor("input_text")))
        assertNull(resolve(Editor("search"), Editor("input")))
    }

    private fun structure(vararg names: String) = FeishuChatStructure().apply {
        names.forEach { observe("com.ss.android.lark:id/$it") }
    }

    @Test fun currentDeviceChatShellIsRecognized() {
        val chat = structure("title", "chat_root", "message_list", "input_text")
        assertTrue(chat.isChat)
        assertTrue(chat.usesNewShell)
    }

    @Test fun partialShellAndGenericTitleDoNotTriggerCapture() {
        assertFalse(structure("title", "input_text").isChat)
        assertFalse(structure("chat_root", "message_list").isChat)
        assertFalse(structure("chat_root", "input_text").isChat)
        assertFalse(structure("message_list", "input_text").isChat)
    }

    @Test fun legacyChatStillWorks() {
        for (marker in listOf("message", "bubble_content_container", "kb_rich_text_content")) {
            val chat = structure(marker)
            assertTrue(chat.isChat)
            assertFalse(chat.usesNewShell)
        }
    }

    @Test fun otherPackagesCannotMatchChatShell() {
        val chat = FeishuChatStructure()
        listOf("chat_root", "message_list", "input_text").forEach {
            chat.observe("com.example:id/$it")
        }
        assertFalse(chat.isChat)
    }
}
