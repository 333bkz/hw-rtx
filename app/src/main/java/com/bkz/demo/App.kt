package com.bkz.demo

import android.app.Application
import com.bkz.hwrtc.rtc

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        rtc.init(this, Constants.appId, "")
    }
}