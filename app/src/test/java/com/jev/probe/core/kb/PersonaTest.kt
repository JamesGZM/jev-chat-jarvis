package com.jev.probe.core.kb

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class PersonaTest {
    @Test fun oldOtherPersonaMergesIntoRelationshipOnce() {
        val old = JSONObject("""{"relationship":"领导","persona":"关注进度","notes":"额外备注"}""")
        val c = ContactJson.read(old, "1")
        assertEquals("领导\n关注进度", c.relationship)
        assertEquals("额外备注", c.notes)
        val saved = ContactJson.write(c)
        assertFalse(saved.has("persona"))
        assertEquals(c, ContactJson.read(saved, "1"))
    }

    @Test fun styleOverrideAndFallbackDoNotChangeIdentityOrRelationship() {
        val contact = Contact("1", "联系人", relationship = "同事", myPersona = "随和", replyStyle = "详细列点")
        val ctx = ChatContext(contact, emptyList(), emptyList(), "严肃", "极简")
        val text = ctx.background("")
        assertTrue(text.contains("我的回复偏好：详细列点"))
        assertFalse(text.contains("极简"))
        assertTrue(text.contains("我的人设（me）：随和"))
        assertEquals("同事", ctx.effectiveRelationship("朋友"))
        val fallback = ctx.copy(contact = contact.copy(replyStyle = " \n ")).background("")
        assertTrue(fallback.contains("我的回复偏好：极简"))
        assertTrue(fallback.contains("我的人设（me）：随和"))
    }

    @Test fun relationshipAndMyPersonaOverrideTheirDefaultsIndependently() {
        val c = Contact("1", "朋友", relationship = "", myPersona = "爱开玩笑")
        val ctx = ChatContext(c, emptyList(), emptyList(), "严肃认真")
        assertEquals("朋友", ctx.effectiveRelationship("朋友"))
        assertTrue(ctx.personaBackground().contains("爱开玩笑"))
        assertFalse(ctx.personaBackground().contains("严肃认真"))
        val roleOnly = ctx.copy(contact = c.copy(relationship = "同事", myPersona = ""))
        assertEquals("同事", roleOnly.effectiveRelationship("朋友"))
        assertTrue(roleOnly.personaBackground().contains("严肃认真"))
    }

    @Test fun earlierFieldIsMigratedButExplicitlyClearingPersonaStaysEmpty() {
        val old = JSONObject("""{"myRole":"旧版内容"}""")
        assertEquals("旧版内容", ContactJson.read(old, "1").myPersona)
        old.put("myPersona", "")
        assertEquals("", ContactJson.read(old, "1").myPersona)
    }

    @Test fun oldContactsKeepNotesAndHaveNoInventedPersona() {
        val c = ContactJson.read(JSONObject("""{"id":"1","name":"联系人","notes":"旧备注","relationship":"同事"}"""), "fallback")
        assertEquals("旧备注", c.notes)
        assertEquals("同事", c.relationship)
        assertEquals("", c.replyStyle)
        assertEquals("", c.myPersona)
    }

    @Test fun allContactFieldsSurviveSavingAndReopening() {
        val c = Contact("1", "联系人", listOf("别名"), listOf("com.tencent.mm"), "领导", "额外备注", "旧摘要", 123L,
            replyStyle = "先说结论", myPersona = "负责客户端交付")
        assertEquals(c, ContactJson.read(JSONObject(ContactJson.write(c).toString()), "fallback"))
        val cleared = c.copy(replyStyle = "", myPersona = "")
        assertEquals(cleared, ContactJson.read(ContactJson.write(cleared), "fallback"))
    }

    @Test fun globalPersonaWorksWithoutMatchedContactOrHistory() {
        val ctx = ChatContext(null, emptyList(), emptyList(), "Android 开发", "先说结论")
        assertFalse(ctx.isEmpty())
        assertTrue(ctx.background("").contains("我的人设（me）：Android 开发"))
        assertTrue(ctx.background("").contains("我的回复偏好：先说结论"))
    }

    @Test fun contactPersonaOverridesOnlyMyGlobalPersonaAndPreservesOtherFields() {
        val c = Contact("1", "联系人", relationship = "老板", notes = "周五同步", replyStyle = "详细说明", myPersona = "客户端负责人")
        val ctx = ChatContext(c, emptyList(), emptyList(), "通用人设", "简短")
        val text = ctx.background("默认关系")
        assertFalse(text.contains("通用人设"))
        for (part in listOf("我的人设（me）：客户端负责人", "我的回复偏好：详细说明", "关系：老板", "周五同步"))
            assertTrue(text.contains(part))
        assertTrue(ctx.personaBackground().contains("客户端负责人"))
        assertFalse(ctx.personaBackground().contains("周五同步"))
    }

    @Test fun blankContactPersonaUsesGlobalAndBlankProfilesAddNothing() {
        val c = Contact("1", "联系人", myPersona = " \n ")
        assertTrue(ChatContext(c, emptyList(), emptyList(), "通用人设").background("").contains("通用人设"))
        val blank = ChatContext(null, emptyList(), emptyList())
        assertTrue(blank.isEmpty())
        assertEquals("", blank.background(""))
        assertEquals("", blank.personaBackground())
    }

    @Test fun contactStyleAloneIsNotEmpty() {
        val ctx = ChatContext(Contact("1", "联系人", replyStyle = "简短"), emptyList(), emptyList())
        assertFalse(ctx.isEmpty())
        assertTrue(ctx.background("").contains("我的回复偏好：简短"))
    }
}
