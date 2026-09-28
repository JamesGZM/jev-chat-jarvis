package com.jev.probe.core.kb

import org.json.JSONArray
import org.json.JSONObject

internal object ContactJson {
    fun read(o: JSONObject, fallbackId: String): Contact = Contact(
        id = o.optString("id").ifBlank { fallbackId },
        name = o.optString("name"),
        aliases = strings(o.optJSONArray("aliases")),
        apps = strings(o.optJSONArray("apps")),
        relationship = listOf(o.optString("relationship"), o.optString("persona"))
            .map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("\n"),
        notes = o.optString("notes"),
        autoSummary = o.optString("autoSummary"),
        updatedAt = o.optLong("updatedAt", 0L),
        replyStyle = o.optString("replyStyle"),
        myPersona = if (o.has("myPersona")) o.optString("myPersona") else o.optString("myRole")
    )

    fun write(c: Contact): JSONObject = JSONObject()
        .put("id", c.id).put("name", c.name)
        .put("aliases", JSONArray(c.aliases)).put("apps", JSONArray(c.apps))
        .put("relationship", c.relationship).put("notes", c.notes)
        .put("autoSummary", c.autoSummary).put("updatedAt", c.updatedAt)
        .put("replyStyle", c.replyStyle).put("myPersona", c.myPersona)

    private fun strings(a: JSONArray?): List<String> =
        if (a == null) emptyList() else (0 until a.length()).map { a.optString(it).trim() }.filter { it.isNotEmpty() }
}
