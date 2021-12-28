package com.bkz.chat

data class ChatModel(
    val userId: String,
    val nickName: String? = null,
    val remarkName: String? = null,
    val avatarUrl: String? = null,
    val content: String? = null,
    val isAnchor: Int = 0,
)