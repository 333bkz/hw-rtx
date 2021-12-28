package com.bkz.chat

interface LiveChatListener {
    fun onMessage(model: ChatModel)
    fun onAnnouncement(model: ChatModel)
}