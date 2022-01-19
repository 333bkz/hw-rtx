package com.bkz.chat

internal const val MESSAGE_TYPE = "messageType"
internal const val MESSAGE = "message"
internal const val SEND_TO = "sendTo"
internal const val SEND_FROM = "sendFrom"
internal const val SESSION_TO = "sessionTo"
internal const val CREATE_TIME = "createTime"
internal const val EPOCH_SECOND = "epochSecond"
internal const val USER_JOIN_NUM = "userJoinNum"
internal const val FROM_FLAG = "flag"

enum class ChatType {
    CHAT,
    JOIN,
    EXIT,
    IMAGE,
    ANNOUNCEMENT,
    TOP_IMAGE,
}

enum class MessageType(val command: String) {
    /**开播*/
    ON_LIVE_START("LIVE_START_EVENT"),

    /**停播*/
    ON_LIVE_END("LIVE_END_EVENT"),

    /**学员加入*/
    ON_JOIN_ROOM("GUEST_JOIN_ROOM"),

    /**学员退出*/
    ON_EXIT_ROOM("GUEST_EXIT"),

    /**学员被禁言*/
    ON_FORBID_CHAT("GUEST_FORBID_CHAT"),

    /**学员解除禁言*/
    ON_RESUME_CHAT("GUEST_RESUME_CHAT"),

    /**学员被拉黑*/
    ON_KICK_OUT("GUEST_BLACK_LIST"),

    /**接收到消息*/
    ON_MSG("GUEST_SEND_MSG"),

    /**助教图片消息*/
    ON_ASSISTANT_IMG("USER_SEND_IMG"),

    /**助教公告*/
    ON_ANNOUNCEMENT("USER_SEND_ANNOUNCEMENT"),

    /**助教置顶图片*/
    ON_TOP_IMG("USER_TOP_IMG"),

    /**备注名修改-只作为命令*/
    ON_REMARK_NAME("USER_REMARK_NAME"),

    /**点赞*/
    ON_UPVOTE("UPVOTE"),

    /**移动播放器*/
    ON_MOVE_PLAYER("USER_MOVE_VIDEO"),

    /**用户列表*/
    ON_GUEST_LIST("LIVE_GUEST_LIST");
}

internal enum class EventType(val command: String) {
    CONNECT("connect"),
    DISCONNECT("disconnect"),
    MESSAGE("message"),
//    ANNOUNCEMENT("showmsg"),
}