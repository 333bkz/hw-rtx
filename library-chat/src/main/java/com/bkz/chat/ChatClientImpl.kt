package com.bkz.chat

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import com.bkz.chat.MessageType.*
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ThreadLocalRandom

val chatClient: ChatClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    ChatClientImpl()
}

private class ChatClientImpl : ChatClient {
    private var socket: Socket? = null
    private var target: ConnectTarget? = null
    private var listener: LiveChatListener? = null
    private val gson: Gson by lazy { Gson() }
    private val handler = Handler(Looper.getMainLooper())
    private val onConnect = Emitter.Listener { listener?.onSocketStateNotify(true) }
    private val onDisConnect = Emitter.Listener { listener?.onSocketStateNotify(false) }
    private val onMessage = Emitter.Listener {
        it?.forEach { msg ->
            if (msg is JSONObject) {
                onMessage(msg)
            }
        }
    }
    private var joinCount = 0
    private val chats = ConcurrentLinkedDeque<ChatModel>()
    private val upvoteState = MutableStateFlow(0)
    private val moveState = MutableStateFlow(PointF(0f, 0f))
    private val chatState = MutableStateFlow<List<ChatModel>>(emptyList())
    private val userState = MutableStateFlow<List<ChatModel>>(emptyList())

    override fun getChatsFlow(): Flow<List<ChatModel>> = chatState.asStateFlow()

    override fun getUpvoteFlow(): Flow<Int> = upvoteState.asStateFlow()

    override fun getUsersFlow(): Flow<List<ChatModel>> = userState.asStateFlow()

    override fun getMoveFlow(): Flow<PointF> = moveState.asStateFlow()

    override fun setChatListener(listener: LiveChatListener) {
        this.listener = listener
    }

    override fun create(url: String, target: ConnectTarget) {
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
        userState.value = emptyList()
        upvoteState.value = 0
        moveState.value = PointF()
        handler.removeCallbacksAndMessages(null)
    }

    override fun sendMessage(content: String): Int = target?.sendCommand(ON_MSG, content) ?: 0

    override fun editRemakeName(content: String): Int =
        target?.sendCommand(ON_REMARK_NAME, content) ?: 0

    override fun upvote(): Int = target?.sendCommand(ON_UPVOTE, "") ?: 0

    private fun ConnectTarget.sendCommand(type: MessageType, content: String? = null): Int {
        if (socket?.connected() != true) return 0
        val command: Command = when (type) {
            ON_MSG -> Command(
                messageType = type.command,
                data = this.also {
                    it.msgId = ThreadLocalRandom.current().nextInt(100_000, 100_0000).toString()
                },
                message = content
            )
            ON_UPVOTE -> Command(
                messageType = type.command,
                data = this,
            )
            ON_REMARK_NAME -> Command(
                messageType = type.command,
                data = this.also {
                    it.msgId = null
                    it.remarkName = content
                },
            )
            else -> return 0
        }
        socket?.emit("msgpub", gson.toJson(command))
        return 1
    }

    private fun onMessage(json: JSONObject) {
        val type = json.optString(MESSAGE_TYPE)
        json.toString().log("-Chat- $type")
        when (type) {
            ON_JOIN_ROOM.command -> {
                if (json.optString(SEND_TO) == target?.guestId) {
                    target?.guestSession = json.optString(SESSION_TO)
                }
                val model = json.chatModel(gson, ChatType.JOIN, SEND_TO)
                model.emitChat()
                execute {
                    if (joinCount != model.onlineCount) {
                        joinCount = model.onlineCount
                        listener?.onGuestCountNotify(joinCount)
                    }
                }
            }
            ON_EXIT_ROOM.command -> {
                val model = json.chatModel(gson, ChatType.EXIT)
                model.emitChat()
                execute {
                    if (joinCount != model.onlineCount) {
                        joinCount = model.onlineCount
                        listener?.onGuestCountNotify(joinCount)
                    }
                }
            }
            ON_GUEST_LIST.command -> {
                userState.value = gson. fromJson(json.optString(MESSAGE), object : TypeToken<List<ChatModel>>() {}.type)
            }
            ON_MOVE_PLAYER.command -> {
                moveState.value = gson.fromJson(json.optString(MESSAGE), PointF::class.java)
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
                json.chatModel(gson, ChatType.CHAT).emitChat()
            }
            ON_ASSISTANT_IMG.command -> {
                json.chatModel(gson, ChatType.IMAGE).emitChat()
            }
            ON_ANNOUNCEMENT.command -> { //公告修改
                execute {
                    listener?.onAnnouncementNotify(json.chatModel(gson, ChatType.ANNOUNCEMENT))
                }
            }
            ON_TOP_IMG.command -> { //置顶图片修改
                execute {
                    listener?.onTopInfoNotify(json.chatModel(gson, ChatType.TOP_IMAGE))
                }
            }
            ON_UPVOTE.command -> {
                upvoteState.value = json.optInt(MESSAGE)
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

    private fun ChatModel.emitChat() {
        synchronized(chats) {
            chats.add(this)
            if (200 < chats.size) {
                chats.removeFirst()
            }
            chatState.value = ArrayList(chats)
        }
    }
}