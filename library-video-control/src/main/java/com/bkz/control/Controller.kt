package com.bkz.control

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView

class Controller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), SeekBar.OnSeekBarChangeListener {

    val container: View
    private val topContainer: View
    private val bottomContainer: View
    private val lock: ImageView
    private val centerPlay: ImageView
    private val back: ImageView
    val title: TextView
    private val switch: ImageView
    val download: ImageView
    private val play: ImageView
    private val current: TextView
    private val progress: SeekBar
    private val duration: TextView
    val speed: TextView
    private val full: ImageView
    private val loading: ENDownloadView
    val seekBar: VideoSeekBar

    init {
        with(inflate(context, R.layout.layout_video_control_view, this)) {
            container = findViewById(R.id.container)
            topContainer = findViewById(R.id.container_top)
            bottomContainer = findViewById(R.id.container_bottom)
            lock = findViewById(R.id.iv_lock)
            centerPlay = findViewById(R.id.iv_center_play)
            back = findViewById(R.id.iv_back)
            title = findViewById(R.id.tv_title)
            download = findViewById(R.id.iv_download)
            switch = findViewById(R.id.iv_switch)
            play = findViewById(R.id.iv_play)
            current = findViewById(R.id.tv_current)
            progress = findViewById(R.id.progress)
            duration = findViewById(R.id.tv_duration)
            speed = findViewById(R.id.tv_speed)
            full = findViewById(R.id.iv_full)
            loading = findViewById(R.id.loading)
            seekBar = findViewById(R.id.video_seek_bar)
        }

        back.onClick {
            itemListener?.onBack()
        }
        switch.onClick(1000) {
            itemListener?.onSwitch()
        }
        download.onClick(1000) {
            itemListener?.onDownload()
        }
        lock.onClick {
            itemListener?.onLock()
        }
        centerPlay.onClick(1000) {
            itemListener?.onPlay()
        }
        play.onClick(1000) {
            itemListener?.onPlay()
        }
        speed.onClick(1000) {
            itemListener?.onSpeed()
        }
        full.onClick(1000) {
            itemListener?.onFull()
        }
        progress.setOnSeekBarChangeListener(this)
    }

    /**
     * 显示中间的播放控制按钮
     */
    var isShowCenterPlay: Boolean = false
        set(value) {
            field = value
            centerPlay.visibleOrGone(value)
            play.visibleOrGone(!value)
        }

    /**
     * 直播隐藏一些按钮
     */
    var isLive: Boolean = false
        set(value) {
            field = value
            if (value) {
                play.gone()
                centerPlay.gone()
                play.gone()
                current.gone()
                progress.gone()
                duration.gone()
                speed.gone()
            }
        }

    /**
     * 锁定界面
     */
    var isLock: Boolean = false
        set(value) {
            field = value
            lock.setImageResource(if (value) R.mipmap.icon_lock else R.mipmap.icon_unlock)
            isActive = value
            active(value)
        }

    /**
     * 播放状态
     */
    var isPlaying: Boolean = false
        set(value) {
            field = value
            play.setImageResource(if (isPlaying) R.mipmap.icon_player_stop else R.mipmap.icon_player_start)
            centerPlay.setImageResource(if (isPlaying) R.mipmap.icon_player_stop else R.mipmap.icon_player_start)
        }

    var isLoading: Boolean = false
        set(value) {
            field = value
            if (value) {
                loading.visible()
                loading.start()
            } else {
                loading.reset()
                loading.gone()
            }
        }

    /**
     * 是否正在活动
     */
    var isActive: Boolean = true
        set(value) {
            field = value
            active(value)
        }

    private fun active(value: Boolean) {
        if (value) {
            topContainer.visibleOrInvisible(!isLock) //锁定不显示
            bottomContainer.visibleOrInvisible(!isLock) //锁定不显示
            centerPlay.visibleOrGone(isShowCenterPlay && !isLock) //锁定不显示
            lock.visibleOrGone(isFull) //全盘显示锁定按钮
        } else {
            topContainer.invisible()
            bottomContainer.invisible()
            centerPlay.gone()
            lock.gone()
        }
    }

    /**
     * 是否全屏
     */
    var isFull: Boolean = false

    var progressValue: Int = 0
        set(value) {
            field = value
            current.text = value.toLong().toTimeSlot()
            progress.progress = value
        }

    var totalValue: Int = 0
        set(value) {
            field = value
            duration.text = value.toLong().toTimeSlot()
            progress.max = value
        }

    var itemListener: ItemClickListener? = null

    override fun onProgressChanged(
        seekBar: SeekBar?, progress: Int, fromUser: Boolean,
    ) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        itemListener?.onSeekTouch()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        itemListener?.onSeekTouchEnd(seekBar)
    }
}

interface ItemClickListener {
    fun onBack()
    fun onDownload()
    fun onSwitch()
    fun onLock()
    fun onPlay()
    fun onFull()
    fun onSpeed()
    fun onSeekTouch()
    fun onSeekTouchEnd(seekBar: SeekBar)
}