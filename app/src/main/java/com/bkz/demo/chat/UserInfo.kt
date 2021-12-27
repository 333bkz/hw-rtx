package com.bkz.demo.chat

data class UserInfo(
    val guestId: String,
    val nickName: String,
    val cellphone: String,
    val avatarUrl: String = "",
    val remarkName: String = "",
    var guestSession:String? = null,
)