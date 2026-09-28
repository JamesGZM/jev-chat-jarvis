package com.jev.probe.capture

/** A late OCR result must never name a different window or message snapshot. */
internal class WindowTitleCache {
    data class Key(val windowId: Int, val messages: String)
    data class Request(val key: Key, val revision: Long)
    private var revision = 0L
    private var key: Key? = null
    private var title: String? = null

    fun invalidate() { revision++; key = null; title = null }
    fun get(current: Key): String? = if (key == current) title else null
    fun begin(current: Key): Request {
        invalidate()
        key = current
        return Request(current, revision)
    }
    fun accepts(request: Request, current: Key?): Boolean =
        request.revision == revision && request.key == key && key == current
    fun save(request: Request, current: Key?, value: String): Boolean {
        if (!accepts(request, current) || value.isBlank()) return false
        title = value.trim()
        return true
    }
}
