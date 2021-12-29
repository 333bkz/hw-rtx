package com.bkz.chat

interface LiveChatListener {
    fun onLiveStart()
    fun onLiveEnd()
    fun onMessage(model: ChatModel)
    fun onGuestCount(count: Int)
    fun onForbidChat()
    fun onResumeChat()
    fun onKickOut()
}

data class ChatModel(
    val content: String? = null,
    var guestId: String? = null,
    val avatarUrl: String? = null,
    val nickName: String? = null,
    val remarkName: String? = null,
    val isAnchor: Int = 0,
)

data class Target(
    val roomNumber: String,
    val guestId: String,
    val nickName: String,
    val cellphone: String? = null,
    val avatarUrl: String? = null,
    var remarkName: String? = null,

    internal var guestSession: String? = null,
    internal var msgId: String? = null,
)

internal data class Command(
    val messageType: String,
    val data: Target,
    val message: String? = null,
)