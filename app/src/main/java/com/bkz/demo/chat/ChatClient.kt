package com.bkz.demo.chat

import android.os.SystemClock
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class ChatClient(
    private val user: UserInfo,
    private val roomId: String,
) {

    private var socket: Socket? = null
    var chatListener: LiveChatListener? = null

    fun create(
        url: String = "wss://su.sxmaps.com:7074/socket.io",
    ) {
        val opts = IO.Options()
        opts.query = user.toOptsQuery()
        opts.transports = arrayOf("websocket", "xhr-polling", "jsonp-polling")
        runCatching {
            socket = IO.socket(url, opts)
        }
    }

    fun connect() {
        socket?.run {
            connect()
            on(EventType.CONNECT.desc, onConnect)
            on(EventType.DISCONNECT.desc, onDisConnect)
            on(EventType.MESSAGE.desc, onMessage)
            on(EventType.ANNOUNCEMENT.desc, onAnnouncement)
        }
    }

    fun disconnect(){
        socket?.disconnect()
    }

    fun sendMessage(msg: String) {
        socket?.emit(MessageType.GUEST_SEND_MSG.name, user.toArrays())
    }

    private var onConnect = Emitter.Listener {

    }

    var onDisConnect = Emitter.Listener {

    }

    var onMessage = Emitter.Listener {

    }

    var onAnnouncement = Emitter.Listener {

    }

    private fun UserInfo.toOptsQuery(): String {
        return "guestId=$guestId&nickName=$nickName&cellphone=$cellphone" +
                "&avatarUrl=$avatarUrl&remarkName=$remarkName" +
                "&roomNumber=$roomId&timeStamp=${SystemClock.currentThreadTimeMillis()}"
    }

    private fun UserInfo.toArrays(append: List<String>? = null): Array<String> {
        return mutableListOf<String>().apply {
            add("guestId:$guestId")
            append?.let {
                addAll(it)
            }
        }.toTypedArray()
    }
}