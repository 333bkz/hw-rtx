package com.bkz.demo

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bkz.chat.*
import com.bkz.control.*
import com.bkz.hwrtc.*
import com.huawei.rtc.models.HRTCStatsInfo
import com.huawei.rtc.utils.HRTCEnums
import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity : AppCompatActivity(), IEventHandler, LiveChatListener {

    private var viewModel: LiveViewModel? = null
    private var mediaController: MediaController? = null
    private val controller: Controller by lazy { Controller(this) }
    private var userSurfaceView: SurfaceView? = null
    private var screenShareSurfaceView: SurfaceView? = null

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
            addView(controller, FrameLayout.LayoutParams(-1, -1))
            mediaController = MediaController(
                this@PlayerActivity, this, controller,
                object : SimpleMediaControllerListener() {
                    override fun onSwitchPosition(isDragUser: Boolean) {
                        this@PlayerActivity.onSwitchPosition(isDragUser)
                    }
                }).also { it.isLive = true }
        }
        registerEventHandler()
        val roomId = "61ca742f8eec9669ed9d6cf0"
        val userId = "6633"
        val userName = "llb2222"
        val cellphone = "18565731244"
        mediaController?.isLoading = true
        //rtc.engine.joinRoom(roomId, Constants.appId, Constants.key, userId, userName)
        viewModel = ViewModelProvider(this).get(LiveViewModel::class.java)
        initChat()
        ChatClient.instance.apply {
            chatListener = this@PlayerActivity
            create(Constants.url, MessageTarget(roomId, userId, userName, cellphone))
            connect()
        }
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
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            || newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ) { //竖屏
            dragContainer.updateLocation(0f, dp2px(48).toFloat())
        } else {
            dragContainer.updateLocation(0f, statusBarHeight.toFloat() + dp2px(48).toFloat())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterEventHandler()
        rtc.engine.leaveRoom()
        ChatClient.instance.clear()
    }

    override fun onWarning(isError: Boolean, code: Int, msg: String) {
    }

    override fun onAuthorizationExpired() {
//        rtc.engine.renewAuthorization()
    }

    override fun onJoinRoomSuccess(isRejoin: Boolean) {
        runOnUiThread {
            mediaController?.isInit = true
        }
    }

    override fun onJoinRoomFailure(code: Int, msg: String) {
    }

    override fun onLeaveRoom(leaveReason: HRTCEnums.HRTCLeaveReason, statsInfo: HRTCStatsInfo) {
    }

    override fun onUserOnline(userId: String) {
        runOnUiThread {
            rtc.engine.online(this, userId).also {
                userSurfaceView = it
                onSwitchPosition(mediaController?.isDragUser == true)
            }
        }
    }

    override fun onUserOffline(userId: String, reason: Int) {
        runOnUiThread {
            rtc.engine.offline(userId)
            userSurfaceView?.let {
                dragContainer.removeView(it)
                screenContainer.removeView(it)
            }
            userSurfaceView = null
        }
    }

    override fun onScreenShareOnline(userId: String) {
        runOnUiThread {
            rtc.engine.screenShareOnline(this, userId).also {
                screenShareSurfaceView = it
                onSwitchPosition(mediaController?.isDragUser == true)
            }
        }
    }

    override fun onScreenShareOffline(userId: String) {
        runOnUiThread {
            rtc.engine.screenShareOffline(userId)
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

    override fun onMessage(model: ChatModel) {
        viewModel?.chatData?.value = model
    }

    override fun onAnnouncement(model: ChatModel) {
        viewModel?.chatData?.value = model
    }
}