package com.bkz.demo.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bkz.chat.ChatModel
import com.bkz.chat.chatClient
import com.bkz.control.onClick
import com.bkz.demo.R
import com.bkz.demo.adapter.ChatItemViewBinder
import com.bkz.demo.vm.LiveViewModel
import com.drakeet.multitype.MultiTypeAdapter
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment() {

    private var viewModel: LiveViewModel? = null
    private val adapter = MultiTypeAdapter()
    private val items = ArrayList<ChatModel>()

    companion object {
        @JvmStatic
        fun newInstance() = ChatFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(LiveViewModel::class.java)
        viewModel?.chatData?.observe(viewLifecycleOwner) {
            items.add(it)
            adapter.notifyItemInserted(items.size)
        }
        items.clear()
        adapter.items = items
        adapter.register(ChatItemViewBinder())
        recycler_view.init(adapter)
        tv_send.onClick(1000) {
            val content = et_content.text.toString().trim()
            if (content.isNotEmpty()) {
                chatClient.sendMessage(content)
            }
            et_content.setText("")
        }
    }

    private fun RecyclerView.init(
        bindAdapter: RecyclerView.Adapter<*>?,
        layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
        isScroll: Boolean = true,
        getItemOffsets: ((position: Int, outRect: Rect) -> Unit)? = null,
    ): RecyclerView {
        this.layoutManager = layoutManager
        //setHasFixedSize(true)
        adapter = bindAdapter
        isNestedScrollingEnabled = isScroll
        overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        getItemOffsets?.let {
            if (itemDecorationCount == 0) {
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State,
                    ) {
                        val position = parent.getChildLayoutPosition(view)
                        it.invoke(position, outRect)
                    }
                })
            }
        }
        return this
    }
}