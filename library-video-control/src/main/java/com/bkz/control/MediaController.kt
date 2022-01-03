package com.bkz.control

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bkz.control.VideoGestureListener.*
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate")
class MediaController(
    private val activity: AppCompatActivity,
    private val parent: View,
    private val controller: Controller,
    private val listener: MediaControllerListener? = null,
) : View.OnTouchListener, ItemClickListener, VideoGestureListener {

    private var systemUiVisibility: Int = 0
    private val screenWidth = activity.screenWidth
    private val speedValue = listOf("1.0x", "1.25x", "1.5x", "2.0x")
    private val handler = Handler(Looper.getMainLooper())
    private val videoGesture: VideoGesture = VideoGesture(activity, this)
    var speedPop: ListPopUpWindow<String>? = null

    init {
        systemUiVisibility = activity.window.decorView.systemUiVisibility
        controller.container.setOnTouchListener(this)
        controller.itemListener = this
    }

    var ratio: Float = 5 / 7f

    var isLive: Boolean = false
        set(value) {
            field = value
            controller.isLive = value
            if (value) {
                controller.download.gone()
            }
        }

    var isInit: Boolean = false
        set(value) {
            field = value
            controller.isLoading = false
            eventUp()
        }

    var isLoading: Boolean = false
        set(value) {
            field = value
            controller.isLoading = value
        }

    var isFullScreen: Boolean = false
        set(value) {
            field = value
            if (!value) { //竖屏
                controller.isLock = false
            }
            handler.post(showRunnable)
            eventUp()
        }

    var isPlaying: Boolean = false
        set(value) {
            field = value
            controller.isPlaying = value
        }

    var title: String? = null
        set(value) {
            field = value
            controller.title.visible()
            controller.title.text = value
        }

    var isLocal: Boolean = false
        set(value) {
            field = value
            controller.download.visibleOrGone(!value)
        }

    var isDragUser: Boolean = true
        private set

    val position: Int
        get() = run {
            controller.progressValue
        }

    val duration: Int
        get() = run {
            controller.totalValue
        }

    private val showRunnable = Runnable { controller.isActive = true }

    private val hideRunnable = Runnable {
        controller.isActive = false
        speedPop?.dismiss()
    }

    private fun eventDown() {
        handler.removeCallbacksAndMessages(null)
        handler.post(if (controller.isActive) hideRunnable else showRunnable)
    }

    private fun eventUp() {
        if (controller.isActive) {
            handler.removeCallbacks(hideRunnable)
            handler.postDelayed(hideRunnable, 5000)
        }
    }

    fun onPlayTimeChange(progress: Int, total: Int) {
        with(controller) {
            progressValue = progress
            totalValue = total
        }
        videoGesture.setTotal(total)
    }

    override fun onBack() {
        activity.onBackPressed()
    }

    override fun onDownload() {
        listener?.onDownload()
    }

    override fun onSwitch() {
        if (isInit) {
            isDragUser = !isDragUser
            listener?.onSwitchPosition(isDragUser)
        }
    }

    override fun onLock() {
        controller.isLock = !controller.isLock
    }

    override fun onPlay() {
        if (isInit && !isLive) {
            if (isPlaying) listener?.stopPlay()
            else listener?.startPlay()
        }
    }

    override fun onFull() {
        with(controller) {
            val lp: ViewGroup.LayoutParams = this@MediaController.parent.layoutParams
            var orientation = activity.requestedOrientation
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                activity.hideSupportActionBar()
                activity.hideNavKey()
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                isFull = true
            } else {
                orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                activity.showSupportActionBar()
                activity.showNavKey(this@MediaController.systemUiVisibility)
                lp.height = (screenWidth * ratio).toInt()
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                isFull = false
                isLock = false //解除锁定
            }
            activity.requestedOrientation = orientation
            this@MediaController.parent.layoutParams = lp
            isFullScreen = isFull
        }
    }

    override fun onSpeed() {
        speedPop?.run {
            setData(speedValue)
            setItemSelectedListener(object : OnItemSelectedListener<String> {
                override fun onItemSelected(value: String) {
                    controller.speed.text = value
                    val speed = value.replace("x", "").toFloat()
                    listener?.onSpeed(speed)
                }
            })
            show(controller.speed)
        }
    }

    override fun onSeekTouch() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onSeekTouchEnd(seekBar: SeekBar) {
        if (isInit) {
            listener?.onSeek(seekBar.progress)
        } else {
            seekBar.progress = 0
        }
    }

    override fun onDoubleTap(): Boolean {
        onPlay()
        return true
    }

    override fun videoGestureCall(type: Int, progress: Int, move: Int) {
        when (type) {
            GESTURE_HORIZONTAL -> if (!isLive && abs(move) >= 5 * 1000) {
                controller.seekBar.showVideoProgress(progress, controller.totalValue)
                listener?.onSeek(progress)
            }
            GESTURE_VERTICAL_LEFT -> controller.seekBar.setProgress(progress).showBrightness()
            GESTURE_VERTICAL_RIGHT -> controller.seekBar.setProgress(progress).showVolume()
            GESTURE_DEFAULT -> {
            }
        }
    }

    override fun isPrepare(): Boolean = isInit

    override fun getCurrentProgress(): Int = controller.progressValue

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!controller.isLock && videoGesture.gestureDetector.onTouchEvent(event)) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> eventDown()
            MotionEvent.ACTION_UP -> eventUp()
        }
        return true
    }
}