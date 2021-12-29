package com.bkz.chat

interface LiveChatListener {
    /**聊天服务状态*/
    fun onActiveStateNotify(isActive: Boolean)

    /**直播状态*/
    fun onLiveActiveStateNotify(isActive: Boolean)

    /**消息*/
    fun onMessageNotify(model: ChatModel)

    /**观看人数*/
    fun onGuestCountNotify(count: Int)

    /**禁言状态*/
    fun onForbidChatNotify(isForbid: Boolean)

    /**被踢*/
    fun onKickOutNotify()
}

data class ChatModel(
    var type: Int = 0,
    var guestId: String? = null,
    var createTime: Int? = null,

    val content: String? = null,
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