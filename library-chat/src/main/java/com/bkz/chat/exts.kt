package com.bkz.chat

import android.util.Log

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