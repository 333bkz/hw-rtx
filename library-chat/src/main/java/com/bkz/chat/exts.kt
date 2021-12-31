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

internal fun JSONObject.chatModel(
    gson: Gson, type: ChatType, guestIdNode: String = SEND_FROM,
): ChatModel {
    return when (type) {
        ChatType.CHAT, ChatType.JOIN, ChatType.EXIT, ChatType.IMAGE, ChatType.TOP_IMAGE -> {
            gson.fromJson(optString(MESSAGE), ChatModel::class.java).also {
                it.type = type
                it.guestId = optString(guestIdNode)
                it.createTime = optJSONObject(CREATE_TIME)?.getInt(EPOCH_SECOND)
                when (type) {
                    ChatType.IMAGE -> {
                        it.content = it.content?.replace("<img src=", "")
                        it.content = it.content?.replace(" alt=\"img\" />", "")
                        it.isAnchor = 1
                    }
                    ChatType.CHAT -> {
                        it.content = it.content?.removePrefix("\n")
                        it.content = it.content?.removeSuffix("\n")
                    }
                    else -> {}
                }
            }
        }
        ChatType.ANNOUNCEMENT -> ChatModel(
            type = type,
            content = optString(MESSAGE),
            userJoinNum = optInt(USER_JOIN_NUM).run { if (this == 0) null else this }
        )
    }
}

internal fun String.removePrefix(target: String): String {
    return if (startsWith(target)) {
        substring(target.length).removePrefix(target)
    } else {
        this
    }
}

internal fun String.removeSuffix(target: String): String {
    return if (endsWith(target)) {
        substring(0, length - target.length).removeSuffix(target)
    } else {
        this
    }
}
