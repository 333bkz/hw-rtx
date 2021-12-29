package com.bkz.demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bkz.chat.ChatModel
import com.bkz.demo.R
import com.bumptech.glide.Glide
import com.drakeet.multitype.ItemViewBinder

class ChatItemViewBinder : ItemViewBinder<ChatModel, ChatItemViewBinder.Holder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater, parent: ViewGroup,
    ) = Holder(inflater.inflate(R.layout.item_chat_layout, parent, false))

    override fun onBindViewHolder(holder: Holder, item: ChatModel) {
        item.apply {
            holder.tv_content.text = content
            holder.tv_name.text = if(remarkName.isNullOrEmpty()) nickName else remarkName
            Glide.with(holder.itemView)
                .load(avatarUrl)
                .placeholder(R.mipmap.icon_default)
                .error(R.mipmap.icon_default)
                .into(holder.iv_head)
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_head: ImageView = itemView.findViewById(R.id.iv_head)
        val iv_content: ImageView = itemView.findViewById(R.id.iv_content)
        val tv_name: TextView = itemView.findViewById(R.id.tv_name)
        val tv_content: TextView = itemView.findViewById(R.id.tv_content)
    }
}