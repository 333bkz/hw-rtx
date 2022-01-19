package com.bkz.demo.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.bkz.control.Controller
import com.bkz.control.MediaController
import com.bkz.control.StatusBarUtil
import com.bkz.demo.R
import kotlinx.android.synthetic.main.activity_player2.*
import sx.ijk.iPlayer.MediaPlayer
import sx.ijk.iPlayer.MediaPlayerObserver
import sx.ijk.iPlayer.TextureRenderView

const val url =
    "https://hw-vod.sxmaps.com/asset/ade0d51080a64d12eec88cbfde97608d/video_test/m_61d7df92fa163edab46914fbf0222128_61dcfdfbd486f77d7563b75d/2022-01-11-05-57-56_2022-01-11-12-28-34.m3u8?auth_info=1vo3C4nxRifv80T9IdnKh9ZbmCwi1wCmocwFaHPpV0YonwHroc8P6ROVs3Z99Y81AOdEyPrPXyyOvYReTbZYIQr4i0GvaYwAm99hnrbgcDDNnYQz29qyOa7tlaHEAeQvW7CtltQn1LLVifUi%2FMBiKe4NvQpqeBNggQTKkBR0ql0%3D.9915c669fc00bba080210997fda81721"
const val url1 = "https://static.sxmaps.com/kdd-file/软件测试/01计算机基础/01课程简介.mp4"
const val url2 =
    "https://hw-vod.sxmaps.com/asset/81be22d9c1b09c9ad346810f4296c074/video_test/m_61d7de63fa163e19fe301a6dcddf55be_61dbf085d486f77d7563b678/2022-01-10-08-42-36_2022-01-10-08-48-46.m3u8?auth_info=fE%2Fs%2B21TD%2FtGgG932V%2Fl2L8gcJtcgQttg1IHbzkFFFpEjvvJJ8F%2BwDBhT5xxOH%2BSDw%2FKN3RVDxMXL3fONp4QKM2dPlBbCC4QiDjN48Kb32wd%2FlzSZ8bAiYMqkce7QXVZHr7N3SmjzlnEqP9N%2BFsRtiw03JZhL33YIrEWg7to%2BLc%3D.07c4e676e9dbebb2a0957e8887c6aff2"
const val path = "/data/user/0/com.bkz.demo/cache/video/m3u85分钟视频文件.m3u8"

class PlayerActivity2 : AppCompatActivity(), MediaPlayerObserver {

    private val handler = Handler(Looper.getMainLooper())
    private var mediaController: MediaController? = null
    private val controller: Controller by lazy { Controller(this) }
    private var mediaPlayer: MediaPlayer? = null
    private var renderView: TextureRenderView? = null
    private val mediaDelegate = MediaControllerListenerImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        super.onCreate(savedInstanceState)
        StatusBarUtil.setTransparentForWindow(this)
        StatusBarUtil.setLightMode(this)
        setContentView(R.layout.activity_player2)
        container.addView(controller, FrameLayout.LayoutParams(-1, -1))
        mediaController = MediaController(this, container, controller, mediaDelegate)
        joinRoom()
    }

    private fun joinRoom() {
        mediaPlayer = MediaPlayer(MediaPlayer.AVParameters(path))
        mediaDelegate.mediaPlayer = mediaPlayer
        mediaPlayer?.setObserver(this)
        mediaPlayer?.init(this)
    }

    override fun onPrepared() {
        if (renderView == null) {
            renderView = TextureRenderView(this)
        }
        container.addView(renderView, 0, FrameLayout.LayoutParams(-1, -1))
        mediaPlayer?.setRenderView(renderView)
        mediaController?.isInit = true
        mediaController?.isPlaying = true
        mediaController?.onPlayTimeChange(0, mediaPlayer?.duration?.toInt() ?: 0)
        Log.e("------", "onPrepared")
    }

    override fun onError(var1: Int, var2: Int) {
        Log.e("------", "onError")
    }

    override fun onVideoSizeChanged(var1: Int, var2: Int) {
        Log.e("------", "onVideoSizeChanged $var1 - $var2")
    }

    override fun onCompletion() {
        Log.e("------", "onCompletion")
    }

    override fun onSeekComplete() {
        Log.e("------", "onSeekComplete")
    }

    override fun onBufferingUpdate(var1: Int) {
        Log.e("------", "onBufferingUpdate $var1")
    }
}