package com.bkz.demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bkz.chat.ChatModel
import com.bkz.demo.R

class ChatAdapter(
    private val data: List<ChatModel>,
) : RecyclerView.Adapter<ChatAdapter.Holder>() {

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_content: ImageView = itemView.findViewById(R.id.iv_content)
        val tv_name: TextView = itemView.findViewById(R.id.tv_name)
        val tv_content: TextView = itemView.findViewById(R.id.tv_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_layout, parent, false)
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        data[position].apply {
            holder.tv_content.text = content
            holder.tv_name.text =
                (if (remarkName.isNullOrEmpty() || remarkName == "null") nickName else remarkName) + ":"
        }
    }

    override fun getItemCount() = data.size
}