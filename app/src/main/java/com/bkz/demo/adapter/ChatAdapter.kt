package com.bkz.demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bkz.chat.ChatModel
import com.bkz.chat.ChatType
import com.bkz.control.gone
import com.bkz.control.visible
import com.bkz.demo.R

class ChatAdapter(
    private val data: List<ChatModel>,
) : RecyclerView.Adapter<ChatAdapter.Holder>() {

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_content: ImageView = itemView.findViewById(R.id.iv_content)
        val tv_content: TextView = itemView.findViewById(R.id.tv_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_layout, parent, false)
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        data[position].apply {
            when (type) {
                ChatType.CHAT -> {
                    holder.iv_content.gone()
                    val remarkName =
                        if (remarkName == "null" || remarkName.isNullOrEmpty()) null else remarkName
                    holder.tv_content.text =
                        String.format("%s: %s", remarkName ?: nickName, content)
                }
                ChatType.JOIN -> {
                    holder.iv_content.gone()
                    holder.tv_content.text = content
                }
                ChatType.IMAGE -> {
                    holder.iv_content.visible()
                    val remarkName =
                        if (remarkName == "null" || remarkName.isNullOrEmpty()) null else remarkName
                    holder.tv_content.text = String.format("%s:", remarkName ?: nickName)
                    holder.iv_content.setImageResource(R.mipmap.ic_launcher)
                }
                else -> {}
            }
        }
    }

    override fun getItemCount() = data.size
}