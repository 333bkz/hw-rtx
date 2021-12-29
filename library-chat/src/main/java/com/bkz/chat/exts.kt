package com.bkz.chat

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject

internal fun String.log(tag: String = "-Chat-") {
    Log.i(tag, this)
}

internal fun Target.toQuery(): String {
    return "guestId=$guestId" +
            "&nickName=$nickName" +
            "&cellphone=$cellphone" +
            "&avatarUrl=$avatarUrl" +
            "&remarkName=$remarkName" +
            "&roomNumber=$roomNumber" +
            "&timeStamp=${System.currentTimeMillis()}"
}

internal fun JSONObject.chatModel(gson: Gson, type: ChatType, node: String = SEND_TO): ChatModel {
    return when (type) {
        ChatType.CHAT, ChatType.JOIN, ChatType.IMAGE -> {
            gson.fromJson(optString(MESSAGE), ChatModel::class.java).also {
                it.type = type
                it.guestId = optString(node)
                it.createTime = optJSONObject(CREATE_TIME)?.getInt(EPOCH_SECOND)
            }
        }
        ChatType.ANNOUNCEMENT -> ChatModel(type = type, content = optString(MESSAGE))
        ChatType.TOP_IMAGE -> ChatModel(
            type = type,
            content = optJSONObject(MESSAGE)?.optString(IMAGE_URL)
        )
    }
}
