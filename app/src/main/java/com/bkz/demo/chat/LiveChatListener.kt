package com.bkz.demo.chat

interface LiveChatListener {
    fun onMessage(model: ChatModel)
    fun onAnnouncement(model: ChatModel)
}