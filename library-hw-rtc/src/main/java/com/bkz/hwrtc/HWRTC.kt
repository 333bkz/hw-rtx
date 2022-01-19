package com.bkz.hwrtc

import android.content.Context
import com.huawei.rtc.HRTCEngine
import com.huawei.rtc.models.HRTCEngineConfig

val rtc: HWRTC by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { HWRTC() }

class HWRTC internal constructor() {

    internal lateinit var engine: HRTCEngine
    internal val handler: RTCEngineEventHandler = RTCEngineEventHandler()

    fun init(context: Context, appId: String, domain: String) {
        engine = HRTCEngine.create(HRTCEngineConfig().also {
            it.context = context.applicationContext
            it.appId = appId
            it.domain = "" //domain
            it.countryCode = ""
            it.isLogEnable = true
            it.logLevel = HRTCEngineConfig.HRTCLogLevel.HRTC_LOG_LEVEL_DEBUG
            it.logPath = context.cacheDir.absolutePath
            it.logSize = 1024 * 10
        }, handler)
    }
}