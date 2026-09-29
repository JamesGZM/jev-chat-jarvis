package com.jev.probe.capture

internal object ManualCaptureFailure {
    fun message(hasRoot: Boolean, emptyRoot: Boolean, recognizedChat: Boolean): String = when {
        !hasRoot -> "暂时无法读取当前窗口，请保持聊天详情页打开后重试；若持续出现，请在助手首页检查读取服务"
        emptyRoot -> "当前页面未向无障碍服务提供内容，无法读取标题和消息。请退出并重新进入聊天；若仍无效，请检查微信或系统的页面保护设置，以及助手首页的读取服务状态"
        !recognizedChat -> "未识别到聊天详情页，请打开具体聊天后重试；若已经在聊天中，当前页面结构可能尚未适配"
        else -> "已识别到聊天页面，但无法读取会话标题。请保持顶部标题可见，并检查设置中的截屏识别开关后重试"
    }
}
