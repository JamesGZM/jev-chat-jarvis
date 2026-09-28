package com.jev.probe.capture

import org.junit.Assert.*
import org.junit.Test

class WindowTitleCacheTest {
    private val key = WindowTitleCache.Key(7, "visible messages")

    @Test fun titleIsAvailableOnlyForMatchingWindowAndMessages() {
        val cache = WindowTitleCache()
        assertTrue(cache.save(cache.begin(key), key, " Alice "))
        assertEquals("Alice", cache.get(key))
        assertNull(cache.get(key.copy(windowId = 8)))
        assertNull(cache.get(key.copy(messages = "different messages")))
    }

    @Test fun navigationRejectsLateResultsEvenAfterReturning() {
        val cache = WindowTitleCache()
        val request = cache.begin(key)
        cache.invalidate()
        cache.begin(key)
        assertFalse(cache.save(request, key, "Old title"))
        assertNull(cache.get(key))
    }

    @Test fun changedContentOrMissingWindowRejectsOcrResult() {
        val cache = WindowTitleCache()
        val request = cache.begin(key)
        assertFalse(cache.save(request, key.copy(messages = "new"), "Alice"))
        assertFalse(cache.save(request, null, "Alice"))
        assertFalse(cache.save(request, key, " "))
    }
}
