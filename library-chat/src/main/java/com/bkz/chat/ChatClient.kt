package com.bkz.chat

import android.graphics.PointF
import kotlinx.coroutines.flow.Flow

interface ChatClient {
    fun setChatListener(listener: LiveChatListener)
    fun create(url: String, target: ConnectTarget)
    fun connect()
    fun sendMessage(content: String): Int
    fun editRemakeName(content: String): Int
    fun upvote(): Int
    fun clear()

    /**消息*/
    fun getChatsFlow(): Flow<List<ChatModel>>

    /**在线学员*/
    fun getUsersFlow(): Flow<List<ChatModel>>

    /**点赞*/
    fun getUpvoteFlow(): Flow<Int>

    /**位置*/
    fun getMoveFlow(): Flow<PointF>
}