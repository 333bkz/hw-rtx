package com.bkz.chat

import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject

class ChatClient private constructor() {

    companion object {
        @JvmStatic
        val instance: ChatClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ChatClient()
        }

        private const val URL = "wss://su.sxmaps.com:7074/im/guest"
    }

    private var socket: Socket? = null
    private val gson: Gson by lazy { Gson() }
    var chatListener: LiveChatListener? = null
    var target: MessageTarget? = null
        private set

    fun create(target: MessageTarget) {
        this.target = target
        val opts = IO.Options()
        opts.timeout = 10_000
        opts.query = target.toQuery()
        opts.transports = arrayOf(WebSocket.NAME)
        runCatching {
            socket = IO.socket(URL, opts)
        }
    }

    fun connect() {
        socket?.run {
            on(EventType.CONNECT.command, onConnect)
            on(EventType.CONNECT_ERROR.command, onConnectError)
            on(EventType.DISCONNECT.command, onDisConnect)
            on(EventType.MESSAGE.command, onMessage)
            on(EventType.ANNOUNCEMENT.command, onAnnouncement)
            connect()
        }
    }

    fun sendMessage(msg: String) {
        if (target == null) {
            return
        }
        socket?.emit(
            EmitType.MSG_PUB.command,
            gson.toJson(Message(
                messageType = MessageType.GUEST_SEND_MSG.name,
                data = target!!,
                message = msg
            ))
        )
    }

    private var onConnect = Emitter.Listener {
        "onConnect".log()
    }

    private var onConnectError = Emitter.Listener {
        "onConnectError".log()
    }

    private var onDisConnect = Emitter.Listener {
        "onDisConnect".log()
    }

    private var onMessage = Emitter.Listener {
        if(!it.isNullOrEmpty()){
            val msg = it[0]
            if(msg is JSONObject) {
                onMessage(msg)
            }
        }
    }

    private var onAnnouncement = Emitter.Listener {
        "onAnnouncement: ${it[0]}".log()
    }

    private  fun onMessage(message:JSONObject){
        message["messageType"].toString().log()
        target?.guestSession = message["sessionTo"].toString()
    }

    fun clear() {
        socket?.off()
        socket?.disconnect()
        socket = null
        target = null
        chatListener = null
    }
}