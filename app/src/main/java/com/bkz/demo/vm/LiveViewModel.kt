package com.bkz.demo.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bkz.chat.ChatModel

class LiveViewModel : ViewModel() {

    val announcement: MutableLiveData<ChatModel> by lazy {
        MutableLiveData()
    }

    val socketState: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    val forbidState: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }
}