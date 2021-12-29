package com.bkz.chat

interface ChatClient {
    fun setLiveChatListener(listener: LiveChatListener)
    fun create(url: String, target: Target)
    fun connect()
    fun sendMessage(content: String)
    fun editRemakeName(content: String)
    fun clear()
}