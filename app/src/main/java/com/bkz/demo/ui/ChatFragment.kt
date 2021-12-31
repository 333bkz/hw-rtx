package com.bkz.demo.ui

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bkz.chat.ChatModel
import com.bkz.chat.chatClient
import com.bkz.demo.R
import com.bkz.demo.adapter.ChatAdapter
import com.bkz.demo.vm.LiveViewModel
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper


class ChatFragment : Fragment() {
    private var viewModel: LiveViewModel? = null
    private val items = ArrayList<ChatModel>()
    private val adapter = ChatAdapter(items)

    companion object {
        @JvmStatic
        fun newInstance() = ChatFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(LiveViewModel::class.java)
        recycler_view.init(adapter)
        recycler_view.overScroll()
        et_content.apply {
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    val input = v.text.toString().trim()
                    if (input.isNotEmpty()) {
                        chatClient.sendMessage(input)
                        et_content.setText("")
                        hideSoftKeyboard(activity)
                    }
                }
                true
            }
        }
        observe()
    }

    private fun observe() {
        viewModel?.forbidState?.observe(viewLifecycleOwner) {
            et_content.isEnabled = it
        }
        viewModel?.socketState?.observe(viewLifecycleOwner) {
            et_content.isEnabled = it
        }
        viewModel?.viewModelScope?.launch {
            chatClient.getChatFlow().collect {
                items.clear()
                items.addAll(it)
                adapter.notifyDataSetChanged()
                scrollToLast()
            }
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

    private fun RecyclerView.overScroll(orientation: Int = OverScrollDecoratorHelper.ORIENTATION_VERTICAL): RecyclerView {
        this.layoutManager?.let {
            OverScrollDecoratorHelper.setUpOverScroll(this, orientation)
        }
        return this
    }

    private fun scrollToLast() {
        val manager: RecyclerView.LayoutManager? = recycler_view.layoutManager
        if (manager is LinearLayoutManager) {
            manager.scrollToPositionWithOffset(adapter.itemCount - 1, 0)
        }
    }

    private fun hideSoftKeyboard(activity: Activity?) {
        activity?.let { act ->
            val view = act.currentFocus
            view?.let {
                val inputMethodManager =
                    act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }
}