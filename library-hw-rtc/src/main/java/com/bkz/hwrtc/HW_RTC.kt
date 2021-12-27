package com.bkz.hwrtc

import android.content.Context
import com.huawei.rtc.HRTCEngine
import com.huawei.rtc.models.HRTCEngineConfig

val rtc: HW_RTC by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { HW_RTC() }

class HW_RTC internal constructor() {

    lateinit var engine: HRTCEngine
    internal var handler: RTCEngineEventHandler? = null

    fun init(context: Context, appId: String, domain: String) {
        handler = RTCEngineEventHandler()
        engine = HRTCEngine.create(HRTCEngineConfig().also {
            it.context = context.applicationContext
            it.appId = appId
            it.domain = "" //domain
            it.countryCode = ""
            it.isLogEnable = true
            it.logLevel = HRTCEngineConfig.HRTCLogLevel.HRTC_LOG_LEVEL_DEBUG
            it.logPath = context.externalCacheDir?.absolutePath ?: context.cacheDir.absolutePath
            it.logSize = 1024 * 10
        }, handler)
    }
}