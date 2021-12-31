package com.bkz.demo.adapter

import androidx.recyclerview.widget.DiffUtil
import com.bkz.chat.ChatModel

class DiffCallBack(
    private val old: List<ChatModel>?,
    private val new: List<ChatModel>?,
) : DiffUtil.Callback() {

    override fun getOldListSize() = old?.size ?: 0
    override fun getNewListSize() = new?.size ?: 0

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = true


    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = false
}
