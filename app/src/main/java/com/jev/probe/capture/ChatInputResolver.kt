package com.jev.probe.capture

/** Called only after validating the live conversation against the analysis token. */
internal fun <T> resolveChatInput(
    pkg: String,
    findById: (String) -> List<T>,
    findUniqueEditable: () -> T?,
    canWrite: (T) -> Boolean
): T? = when (pkg) {
    "com.tencent.mobileqq" -> findById("com.tencent.mobileqq:id/input").firstOrNull()
    "com.ss.android.lark" -> resolveFeishuInput(findById, canWrite)
    "com.tencent.mm", "com.twitter.android" -> findUniqueEditable()
    else -> null
}
