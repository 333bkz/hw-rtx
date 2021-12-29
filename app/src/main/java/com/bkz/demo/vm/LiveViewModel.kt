package com.bkz.demo.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bkz.chat.ChatModel

class LiveViewModel : ViewModel() {

    val chatData: MutableLiveData<ChatModel> by lazy {
        MutableLiveData()
    }

}