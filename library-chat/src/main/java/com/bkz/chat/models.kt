package com.bkz.chat

interface LiveChatListener {
    /**聊天服务状态*/
    fun onSocketStateNotify(isActive: Boolean) {}

    /**直播状态*/
    fun onLiveStateNotify(isActive: Boolean) {}

    /**禁言状态*/
    fun onForbidChatNotify(isForbid: Boolean) {}

    /**被踢*/
    fun onKickOutNotify() {}

    /**观看人数*/
    fun onGuestCountNotify(count: Int) {}

    /**公告*/
    fun onAnnouncementNotify(model: ChatModel) {}
}

data class ChatModel(
    /**消息类型*/
    var type: ChatType = ChatType.CHAT,
    /**用户ID*/
    var guestId: String? = null,
    /**创建时间*/
    var createTime: Int? = null,
    /**CHAT=消息内容 ｜ IMAGE=图片 ｜ JOIN=用户进入消息 ｜ ANNOUNCEMENT=公告*/
    var content: String? = null,
    /**TOP_IMAGE=置顶图片*/
    var imageUrl: String? = null,
    /**头像*/
    var avatarUrl: String? = null,
    /**名字*/
    var nickName: String? = null,
    /**备注*/
    var remarkName: String? = null,
    /**老师*/
    var fromType: Int = 0,
    /**ANNOUNCEMENT=用户数量*/
    var userJoinNum: Int? = null,
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