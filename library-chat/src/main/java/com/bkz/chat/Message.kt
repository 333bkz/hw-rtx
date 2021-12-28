package com.bkz.chat

data class Message(
    val messageType: String,
    val data: MessageTarget,
    val message: String,
)

data class MessageTarget(
    val roomNumber: String,
    val guestId: String,
    val nickName: String,
    val cellphone: String,
    val avatarUrl: String = "",
    val remarkName: String = "",
    var guestSession: String? = null,
    val msgId: String? = null,
) {
    fun toQuery(): String {
        return "guestId=$guestId" +
                "&nickName=$nickName" +
                "&cellphone=$cellphone" +
                "&avatarUrl=" +
                "&remarkName=" +
                "&roomNumber=$roomNumber" +
                "&timeStamp=${System.currentTimeMillis()}"
    }
}