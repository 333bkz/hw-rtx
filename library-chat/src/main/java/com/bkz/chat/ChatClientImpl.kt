package com.bkz.chat

import android.os.Handler
import android.os.Looper
import com.bkz.chat.MessageType.*
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import org.json.JSONObject
import java.util.concurrent.ThreadLocalRandom

val chatClient: ChatClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    ChatClientImpl()
}

private class ChatClientImpl : ChatClient {
    private var socket: Socket? = null
    private var target: Target? = null
    private var listener: LiveChatListener? = null
    private val gson: Gson by lazy { Gson() }
    private val handler = Handler(Looper.getMainLooper())
    private val onConnect = Emitter.Listener { listener?.onSocketStateNotify(true) }
    private val onDisConnect = Emitter.Listener { listener?.onSocketStateNotify(false) }
    private val onMessage = Emitter.Listener {
        if (!it.isNullOrEmpty()) {
            with(it[0]) {
                if (this is JSONObject) {
                    onMessage(this)
                }
            }
        }
    }
    private var joinCount = 0
    private var lastSend = 0L
    private val chats = mutableListOf<ChatModel>()
    private val chatState = MutableStateFlow<List<ChatModel>>(emptyList())

    override fun setLiveChatListener(listener: LiveChatListener) {
        this.listener = listener
    }

    override fun create(url: String, target: Target) {
        this.target = target
        val opts = IO.Options()
        opts.forceNew = true
        opts.timeout = 10_000
        opts.query = target.toQuery()
        opts.transports = arrayOf(WebSocket.NAME)
        opts.extraHeaders = mapOf(Pair("user-agent", listOf(System.getProperty("http.agent"))))
        runCatching { socket = IO.socket(url, opts) }
    }

    override fun connect() {
        socket?.run {
            on(EventType.CONNECT.command, onConnect)
            on(EventType.DISCONNECT.command, onDisConnect)
            on(EventType.MESSAGE.command, onMessage)
            connect()
        }
    }

    override fun clear() {
        socket?.off()
        socket?.disconnect()
        socket = null
        target = null
        listener = null
        joinCount = 0
        chats.clear()
        chatState.value = emptyList()
        handler.removeCallbacksAndMessages(null)
    }

    override fun sendMessage(content: String) {
        target?.sendCommand(ON_MSG, content)
    }

    override fun editRemakeName(content: String) {
        target?.sendCommand(ON_REMARK_NAME, content)
    }

    private fun Target.sendCommand(type: MessageType, content: String? = null) {
        if (socket?.connected() != true) {
            return
        }
        val command: Command? = when (type) {
            ON_GUEST_COUNT -> Command(
                messageType = type.command,
                data = this.also {
                    it.msgId = null
                },
            )
            ON_MSG -> Command(
                messageType = type.command,
                data = this.also {
                    it.msgId = ThreadLocalRandom.current().nextInt(100_000, 100_0000).toString()
                },
                message = content
            )
            ON_REMARK_NAME -> Command(
                messageType = type.command,
                data = this.also {
                    it.msgId = null
                    it.remarkName = content
                },
            )
            else -> null
        }
        command?.let {
            socket?.emit("msgpub", gson.toJson(it))
        }
    }

    private fun onMessage(json: JSONObject) {
        val type = json.optString(MESSAGE_TYPE)
        json.toString().log("-Chat- $type")
        when (type) {
            ON_GUEST_COUNT.command -> {
                val count = json.optInt(MESSAGE)
                if (count != joinCount) {
                    joinCount = count
                    execute {
                        listener?.onGuestCountNotify(joinCount)
                    }
                }
            }
            ON_JOIN_ROOM.command -> {
                if (json.optString(SEND_TO) == target?.guestId) {
                    target?.guestSession = json.optString(SESSION_TO)
                }
                json.chatModel(gson, ChatType.JOIN, SEND_TO).send()
            }
            ON_EXIT_ROOM.command -> {
                val cur = System.currentTimeMillis()
                if (cur - lastSend > 500) {
                    lastSend = cur
                    target?.sendCommand(ON_GUEST_COUNT)
                }
                json.chatModel(gson, ChatType.EXIT).send()
            }
            ON_FORBID_CHAT.command -> execute {
                listener?.onForbidChatNotify(true)
            }
            ON_RESUME_CHAT.command -> execute {
                listener?.onForbidChatNotify(false)
            }
            ON_KICK_OUT.command -> execute {
                listener?.onKickOutNotify()
            }
            ON_MSG.command -> {
                json.chatModel(gson, ChatType.CHAT).send()
            }
            ON_ASSISTANT_IMG.command -> {
                json.chatModel(gson, ChatType.IMAGE).send()
            }
            ON_ANNOUNCEMENT.command -> {
                val model = json.chatModel(gson, ChatType.ANNOUNCEMENT)
                execute {
                    listener?.onAnnouncementNotify(model)
                }
            }
            ON_TOP_IMG.command -> {
                val model = json.chatModel(gson, ChatType.TOP_IMAGE)
                execute {
                    listener?.onAnnouncementNotify(model)
                }
            }
            ON_LIVE_START.command -> execute {
                listener?.onLiveStateNotify(true)
            }
            ON_LIVE_END.command -> execute {
                listener?.onLiveStateNotify(false)
            }
            else -> {}
        }
    }

    private fun execute(it: () -> Unit) {
        handler.post { it.invoke() }
    }

    private fun ChatModel.send() {
        synchronized(chats) {
            chats.add(this)
            if (chats.size > 200) {
                chats.removeAt(0)
            }
            chatState.value = ArrayList(chats)
        }
    }

    override fun getChatsFlow(): Flow<List<ChatModel>> =
        chatState.buffer(0, BufferOverflow.DROP_OLDEST)
}