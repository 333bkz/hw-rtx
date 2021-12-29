package com.bkz.demo.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
import com.huawei.rtc.models.HRTCStatsInfo
import com.huawei.rtc.utils.HRTCEnums
import kotlinx.android.synthetic.main.activity_player.*

val target = Target("61cc2a0e8eec9669ed9d9fec", "6633", "llb2222", "18565731244")

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
        viewModel = ViewModelProvider(this).get(LiveViewModel::class.java)
        initChat()
        chatClient.apply {
            setLiveChatListener(this@PlayerActivity)
            create(Constants.url, target)
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
        leaveRoom()
        chatClient.clear()
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

    private fun leaveRoom() {
        rtc.engine.leaveRoom()
    }

    override fun onActiveStateNotify(isActive: Boolean) {

    }

    override fun onLiveActiveStateNotify(isActive: Boolean) {
        if (isActive) {
            runOnUiThread {
                mediaController?.isLoading = true
            }
            rtc.engine.joinRoom(
                target.roomNumber,
                Constants.appId,
                Constants.key,
                target.guestId,
                target.nickName,
            )
        } else {
            runOnUiThread {
                Toast.makeText(this, "直播结束", Toast.LENGTH_SHORT).show()
            }
            leaveRoom()
        }
    }

    override fun onMessageNotify(model: ChatModel) {
        Log.i("onMessageNotify", model.toString())
        when (model.type) {
            ChatType.CHAT -> {
                viewModel?.chatData?.postValue(model)
            }
            ChatType.JOIN -> {

            }
            ChatType.IMAGE -> {

            }
            ChatType.ANNOUNCEMENT -> {

            }
            ChatType.TOP_IMAGE -> {

            }
        }
    }

    override fun onGuestCountNotify(count: Int) {
        runOnUiThread {
            tv_count.text = "聊天室： $count"
        }
    }

    override fun onForbidChatNotify(isForbid: Boolean) {
    }

    override fun onKickOutNotify() {
        leaveRoom()
        chatClient.clear()
        runOnUiThread {
            Toast.makeText(this, "被踢出", Toast.LENGTH_SHORT).show()
        }
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