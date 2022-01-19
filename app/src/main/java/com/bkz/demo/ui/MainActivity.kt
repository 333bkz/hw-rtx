package com.bkz.demo.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bkz.control.onClick
import com.bkz.demo.R
import com.bkz.downloader.FileInfo
import com.bkz.downloader.SxDownloader

class MainActivity : AppCompatActivity() {

    private val mPermissions = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE
    )

    private val fileInfo = FileInfo(
        fileId = 100,
        fileUrl = "https://hw-vod.sxmaps.com/asset/81be22d9c1b09c9ad346810f4296c074/video_test/m_61d7de63fa163e19fe301a6dcddf55be_61dbf085d486f77d7563b678/2022-01-10-08-42-36_2022-01-10-08-48-46.m3u8?auth_info=ZEWD9SZ%2FWm1XdB5QtvXisHf3mely4JkPZW%2F9J49jbQ2k%2FQp0UU44FgLt3iF7nw6PxtgVUncJyl704U2MvbdWXC9TYVnP5hf6WI9ZrJO%2BzkdpeBLZKyFn7fdo76FzWrPzBQir2lpPScaEGoLoab1ilFd5CAjqnyvE8aLPJVy90CI%3D.338d2dca5e1038e598907babe2e275b0",
        courseNo = "1",
        fileName = "m3u85分钟视频文件",
        fileSuffix = "m3u8",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(mPermissions, 0x1234)
        }
        findViewById<View>(R.id.playerBtn).onClick {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        findViewById<View>(R.id.playerBtn2).onClick {
            startActivity(Intent(this, PlayerActivity2::class.java))
        }
        findViewById<View>(R.id.download).onClick {
            SxDownloader.instance.start(fileInfo.fileUrl ?: "", fileInfo)
        }
        findViewById<View>(R.id.stop).onClick {
            SxDownloader.instance.pause(fileInfo)
        }
        findViewById<View>(R.id.cancel).onClick {
            SxDownloader.instance.cancel(fileInfo)
        }
        findViewById<View>(R.id.delete).onClick {
            SxDownloader.instance.delete(fileInfo)
        }
    }
}