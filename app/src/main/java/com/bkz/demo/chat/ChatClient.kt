package com.bkz.demo.chat

import android.os.SystemClock
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class ChatClient(
    private val target: MessageTarget,
) {

    private var socket: Socket? = null
    var chatListener: LiveChatListener? = null

    fun create(
        url: String = "wss://su.sxmaps.com:7074/socket.io",
    ) {
        val opts = IO.Options()
        opts.query = target.toOptsQuery()
        opts.transports = arrayOf("websocket", "xhr-polling", "jsonp-polling")
        runCatching {
            socket = IO.socket(url, opts)
        }
    }

    fun connect() {
        socket?.run {
            connect()
            on(EventType.CONNECT.command, onConnect)
            on(EventType.DISCONNECT.command, onDisConnect)
            on(EventType.MESSAGE.command, onMessage)
            on(EventType.ANNOUNCEMENT.command, onAnnouncement)
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun sendMessage(msg: String) {
        socket?.emit(
            EmitType.MSG_PUB.command,
            Message(
                messageType = MessageType.GUEST_SEND_MSG.name,
                data = target,
                message = msg
            )
        )
    }

    private var onConnect = Emitter.Listener {

    }

    private var onDisConnect = Emitter.Listener {

    }

    private var onMessage = Emitter.Listener {

    }

    private var onAnnouncement = Emitter.Listener {

    }

    private fun MessageTarget.toOptsQuery(): String {
        return "guestId=$guestId&nickName=$nickName&cellphone=$cellphone" +
                "&avatarUrl=$avatarUrl&remarkName=$remarkName" +
                "&roomNumber=$roomNumber&timeStamp=${SystemClock.currentThreadTimeMillis()}"
    }

    private fun MessageTarget.toArrays(append: List<String>? = null): Array<String> {
        return mutableListOf<String>().apply {
            add("guestId:$guestId")
            append?.let {
                addAll(it)
            }
        }.toTypedArray()
    }
}