package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class ChatInputResolverTest {
    @Test fun wechatUsesTheVerifiedUniqueEditor() {
        assertEquals("wechat-editor", resolveChatInput("com.tencent.mm", { emptyList<String>() }, { "wechat-editor" }, { true }))
    }

    @Test fun missingOrAmbiguousWechatEditorDoesNotWrite() {
        assertNull(resolveChatInput<String>("com.tencent.mm", { emptyList() }, { null }, { true }))
    }

    @Test fun unknownAppCannotReachGenericEditorLookup() {
        val input = resolveChatInput<String>("com.example.unknown", { error("unexpected lookup") },
            { error("unadapted app must not write") }, { true })
        assertNull(input)
    }

    @Test fun qqAndFeishuRetainDedicatedEditors() {
        val ids = setOf("com.tencent.mobileqq:id/input", "com.ss.android.lark:id/input_text")
        for (pkg in listOf("com.tencent.mobileqq", "com.ss.android.lark")) {
            val input = resolveChatInput(pkg, { if (it in ids) listOf(it) else emptyList() },
                { error("must use dedicated editor") }, { true })
            assertTrue(input in ids)
        }
    }
}
