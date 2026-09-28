package com.jev.probe.core.kb

import org.junit.Assert.*
import org.junit.Test

class ChatContextTest {
    private val global = "对方是我的伴侣"
    private fun context(relationship: String) = ChatContext(
        Contact(id = "test", name = "Example", relationship = relationship), emptyList(), emptyList()
    )

    @Test fun bossContactOverridesPartnerDefault() {
        val ctx = context(" 老板 ")
        val effective = ctx.effectiveRelationship(global)
        assertEquals("老板", effective)
        assertTrue(ctx.background(effective).contains("关系：老板"))
        assertFalse(ctx.background(effective).contains("伴侣"))
    }

    @Test fun emptyContactRelationshipFallsBackToGlobal() {
        assertEquals(global, context("").effectiveRelationship(global))
        assertEquals(global, context(" \n ").effectiveRelationship(global))
    }

    @Test fun unmatchedContactUsesGlobal() {
        assertEquals(global, ChatContext(null, emptyList(), emptyList()).effectiveRelationship(global))
    }

    @Test fun relationshipOverrideDoesNotDependOnStoredHistory() {
        val ctx = context("老板")
        assertTrue(ctx.history.isEmpty())
        assertEquals("老板", ctx.effectiveRelationship(global))
    }
}
