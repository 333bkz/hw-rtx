package com.bkz.chat

import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class JWebSocketClient(uri: URI) : WebSocketClient(uri, Draft_6455()) {

    override fun onOpen(handshakedata: ServerHandshake) {
        "onOpen ${handshakedata.httpStatus} - ${handshakedata.httpStatusMessage}".log()
    }

    override fun onMessage(message: String) {
        "onMessage $message".log()
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        "onClose: $code - $reason -$remote".log()
    }

    override fun onError(ex: Exception) {
        "onError ${ex.message}".log()
    }
}