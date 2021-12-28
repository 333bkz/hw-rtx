package com.bkz.chat

enum class EventType(val command: String) {
    CONNECT("connect"),
    DISCONNECT("disconnect"),
    CONNECT_ERROR("connect_error"),
    MESSAGE("message"),
    ANNOUNCEMENT("showmsg"),
}

enum class MessageType {
    LIVE_START_EVENT,//开播
    LIVE_END_EVENT,//停播
    GUEST_JOIN_ROOM,//加入
    GUEST_FORBID_CHAT,//学员被禁言
    GUEST_RESUME_CHAT,//学员解除禁言
    GUEST_BLACK_LIST,//学员被拉黑
    GUEST_SEND_MSG,//学员消息
    USER_SEND_IMG,//老师图片消息
    USER_SEND_MSG,//老师消息
    USER_SEND_ANNOUNCEMENT,//公告
    USER_TOP_IMG,//置顶图片
}

enum class EmitType(val command: String) {
    MSG_PUB("msgpub"),
}