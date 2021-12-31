package com.bkz.chat

import kotlinx.coroutines.flow.Flow

interface ChatClient {
    fun setLiveChatListener(listener: LiveChatListener)
    fun create(url: String, target: Target)
    fun connect()
    fun sendMessage(content: String)
    fun editRemakeName(content: String)
    fun clear()

    /**
     * JOIN + EXIT + CHAT + IMAGE
     */
    fun getChatsFlow(): Flow<List<ChatModel>>
}