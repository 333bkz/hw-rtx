package com.bkz.chat

internal enum class EventType(val command: String) {
    CONNECT("connect"),
    DISCONNECT("disconnect"),
    CONNECT_ERROR("connect_error"),
    MESSAGE("message"),
    ANNOUNCEMENT("showmsg"),
}

internal enum class MessageType(val command: String) {
    /**开播*/
    ON_LIVE_START("LIVE_START_EVENT"),

    /**停播*/
    ON_LIVE_END("LIVE_END_EVENT"),

    /**学员人数*/
    ON_GUEST_COUNT("LIVE_GUEST_COUNT"),

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

    /**助教消息*/
    ON_ASSISTANT_MSG("USER_SEND_MSG"),

    /**助教图片消息*/
    ON_ASSISTANT_IMG("USER_SEND_IMG"),

    /**助教公告*/
    ON_ANNOUNCEMENT("USER_SEND_ANNOUNCEMENT"),

    /**助教置顶图片*/
    ON_TOP_IMG("USER_TOP_IMG"),

    /**备注名修改*/
    ON_REMARK_NAME("USER_REMARK_NAME");
}