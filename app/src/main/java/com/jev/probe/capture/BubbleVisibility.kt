package com.jev.probe.capture

/** Visibility follows the foreground app, not whether a chat can be parsed. */
internal class BubbleVisibility(private val ownPackage: String) {
    enum class Decision { SHOW, HIDE, KEEP }
    private var hiddenPackage: String? = null

    fun hideForVisit(pkg: String?) { hiddenPackage = pkg }
    fun restore() { hiddenPackage = null }

    fun decide(enabled: Boolean, pkg: String?): Decision {
        if (pkg != null && pkg != hiddenPackage) hiddenPackage = null
        if (!enabled) return Decision.HIDE
        if (pkg == null) return Decision.KEEP
        if (pkg == ownPackage || pkg == "com.android.systemui" || pkg == "com.miui.home" ||
            pkg.contains("launcher", ignoreCase = true) || pkg == hiddenPackage) return Decision.HIDE
        return Decision.SHOW
    }
}
