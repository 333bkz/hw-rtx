package com.bkz.demo.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bkz.chat.*
import com.bkz.control.*
import com.bkz.demo.Constants
import com.bkz.demo.R
import com.bkz.demo.vm.LiveViewModel
import com.bkz.hwrtc.*
import kotlinx.android.synthetic.main.activity_player.*

val target = ConnectTarget("61d5632dc373ac6b56c61792", "11111", "xxxx", "")

class PlayerActivity : AppCompatActivity(), IEventHandler, LiveChatListener {

    private val handler = Handler(Looper.getMainLooper())
    private var viewModel: LiveViewModel? = null
    private var mediaController: MediaController? = null
    private val controller: Controller by lazy { Controller(this) }
    private var userSurfaceView: SurfaceView? = null
    private var screenShareSurfaceView: SurfaceView? = null
    private val portrait = Point()
    private val landscape = Point()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )//打开硬件加速
        super.onCreate(savedInstanceState)
        StatusBarUtil.setTransparentForWindow(this)
        StatusBarUtil.setLightMode(this)
        setContentView(R.layout.activity_player)
        screenContainer.apply {
            layoutParams = LinearLayout.LayoutParams(-1, screenWidth * 5 / 7)
            val width = dp2px(120)
            val height = dp2px(100)
            portrait.x = screenWidth - width
            portrait.y = statusBarHeight + screenWidth * 5 / 7
            landscape.x = screenHeight - width
            landscape.y = screenWidth - height
            addView(controller, FrameLayout.LayoutParams(-1, -1))
            mediaController = MediaController(
                this@PlayerActivity, this, controller,
                object : MediaControllerListener {
                    override fun onSwitchPosition(isDragUser: Boolean) {
                        this@PlayerActivity.onSwitchPosition(isDragUser)
                    }
                }).also { it.isLive = true }
        }
        registerEventHandler()
        viewModel = ViewModelProvider(this).get(LiveViewModel::class.java)
        chatClient.apply {
            setChatListener(this@PlayerActivity)
            create(Constants.url, target)
            connect()
        }
        initChat()
    }

    private fun initChat() {
        view_pager?.apply {
            this.adapter = object : FragmentStateAdapter(this@PlayerActivity) {
                override fun getItemCount() = 1
                override fun createFragment(position: Int) = ChatFragment.newInstance()
            }
        }
    }

    override fun onBackPressed() {
        mediaController?.apply {
            if (isFullScreen) {
                onFull()
                return@onBackPressed
            }
        }
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handler.postDelayed({
            if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) { //竖屏
                dragContainer.updateLocationForMove(PointF(portrait), 300)
            } else {
                dragContainer.updateLocationForMove(PointF(landscape), 300)
            }
        }, 600)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterEventHandler()
        leaveRoom()
        chatClient.clear()
    }

    override fun onJoinRoomSuccess(isRejoin: Boolean) {
        runOnUiThread {
            mediaController?.isInit = true
        }
    }

    override fun onUserOnline(userId: String) {
        runOnUiThread {
            rtc.online(this, userId).also {
                userSurfaceView = it
                onSwitchPosition(mediaController?.isDragUser == true)
            }
        }
    }

    override fun onUserOffline(userId: String, reason: Int) {
        runOnUiThread {
            rtc.offline(userId)
            userSurfaceView?.let {
                dragContainer.removeView(it)
                screenContainer.removeView(it)
            }
            userSurfaceView = null
        }
    }

    override fun onScreenShareOnline(userId: String) {
        runOnUiThread {
            rtc.screenShareOnline(this, userId).also {
                screenShareSurfaceView = it
                onSwitchPosition(mediaController?.isDragUser == true)
            }
        }
    }

    override fun onScreenShareOffline(userId: String) {
        runOnUiThread {
            rtc.screenShareOffline(userId)
            screenShareSurfaceView?.let {
                dragContainer.removeView(it)
                screenContainer.removeView(it)
            }
            screenShareSurfaceView = null
        }
    }

    /**
     * SurfaceView拥有一个独立于Activity之外的Window
     * SurfaceView被设置为不可见的时候，其所对应的Window就会销毁，再想去显示这个SurfaceView的时候必须重新创建
     * 多个SurfaceView在FrameLayout按创建先后顺序显示，不会按照View的添加顺序显示
     * 使用setZOrderOnTop(true)设置显示顺序
     */
    private fun onSwitchPosition(isDragUser: Boolean) {
        userSurfaceView?.setZOrderOnTop(isDragUser)
        screenShareSurfaceView?.setZOrderOnTop(!isDragUser)
        userSurfaceView?.let {
            if (isDragUser) {
                screenContainer.removeView(it)
                dragContainer.addChild(it)
            } else {
                dragContainer.removeView(it)
                screenContainer.addChild(it)
            }
        }
        screenShareSurfaceView?.let {
            if (isDragUser) {
                dragContainer.removeView(it)
                screenContainer.addChild(it)
            } else {
                screenContainer.removeView(it)
                dragContainer.addChild(it)
            }
        }
    }

    private fun ViewGroup.addChild(view: View, index: Int = 0) {
        if (contains(view)) {
            return
        }
        val param = FrameLayout.LayoutParams(-1, -1)
        addView(view, index, param)
    }

    private fun leaveRoom() {
        rtc.leaveRoom()
    }

    override fun onLiveStateNotify(isActive: Boolean) {
        if (isActive) {
            mediaController?.isLoading = true
            rtc.joinRoom(
                target.roomNumber,
                Constants.appId,
                Constants.key,
                target.guestId,
                target.nickName,
            )
        } else {
            Toast.makeText(this, "直播结束", Toast.LENGTH_SHORT).show()
            leaveRoom()
        }
    }

    override fun onAnnouncementNotify(chat: ChatModel) {
        viewModel?.announcement?.value = chat
    }

    override fun onTopInfoNotify(chat: ChatModel) {

    }

    override fun onGuestCountNotify(count: Int) {
    }

    override fun onKickOutNotify() {
        leaveRoom()
        chatClient.clear()
        Toast.makeText(this, "您已经被踢出", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (currentFocus.isShouldHideInput(ev)) {
                hideSoftKeyboard()
                currentFocus?.clearFocus()
            }
        }
        runCatching {
            return super.dispatchTouchEvent(ev)
        }
        return false
    }
}