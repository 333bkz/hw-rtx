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

    /**真实观看人数*/
    fun onGuestCountNotify(count: Int) {}

    /**置顶信息*/
    fun onTopInfoNotify(chat: ChatModel) {}

    /**公告 + 额外观看人数*/
    fun onAnnouncementNotify(chat: ChatModel) {}
}

data class ChatModel(
    /**消息类型*/
    var type: ChatType = ChatType.CHAT,
    /**用户ID*/
    var guestId: String? = null,
    /**创建时间*/
    var msgTime: Int? = null,
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
    /**老师 1是老师，2是助教， 0学员*/
    var fromType: Int = 0,
    /**ANNOUNCEMENT=用户数量*/
    var userJoinNum: Int = 0,
    /**ON_JOIN_ROOM,ON_EXIT_ROOM=用户真实数量*/
    var onlineCount: Int = 0,
)

data class ConnectTarget(
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
    val data: ConnectTarget,
    val message: String? = null,
)