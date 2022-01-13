package com.bkz.demo.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bkz.control.onClick
import com.bkz.demo.R

class MainActivity : AppCompatActivity() {

    private val mPermissions = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.playerBtn).onClick {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        findViewById<View>(R.id.playerBtn2).onClick {
            startActivity(Intent(this, PlayerActivity2::class.java))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(mPermissions, 0x1234)
        }
    }
}