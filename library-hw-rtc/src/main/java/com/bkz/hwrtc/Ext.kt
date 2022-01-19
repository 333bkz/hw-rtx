package com.bkz.hwrtc

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.huawei.rtc.models.HRTCJoinParam
import com.huawei.rtc.utils.HRTCConstants
import com.huawei.rtc.utils.HRTCEnums.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal fun String.log(tag: String = "-HW-RTC-") {
    Log.i(tag, this)
}

//注册监听
fun IEventHandler.registerEventHandler() {
    rtc.handler.handler = this
}

//注销监听
fun unregisterEventHandler() {
    rtc.handler.handler = null
}

fun HWRTC.leaveRoom() {
    engine.leaveRoom()
}

//接收对应远端用户的音频流
fun HWRTC.openRemoteAudio(userId: String) {
    engine.muteRemoteAudio(userId, false)
}

//关闭接收对应远端用户的音频流
fun HWRTC.cancelRemoteAudio(userId: String) {
    engine.muteRemoteAudio(userId, true)
}

//加入房间
fun HWRTC.joinRoom(
    roomId: String,
    appId: String,
    key: String,
    userId: String,
    userName: String,
) = engine.joinRoom(HRTCJoinParam().apply {
    this.userId = userId
    this.userName = userName
    this.role = HRTCJoinParam.HRTCRoleType.HRTC_ROLE_TYPE_PLAYER
    this.roomId = roomId
    this.sfuType = 0
    this.optionalInfo = ""
    this.autoSubscribeAudio = true
    this.autoSubscribeVideo = true
    this.ctime = System.currentTimeMillis() / 1000 + 60 * 60 //有效时间为1小时，单位是秒
    val content = "$appId+$roomId+$userId+${this.ctime}"
    this.authorization = hmacSha(key, content)
}) == HRTCConstants.HRTC_SUCCESS

/**
 * 订阅对应用户并获取流视图
 *
 * @param displayMode [HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_FIT]  (不拉伸）黑边模式，通过扩边的方式保持宽高比
 * [HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_HIDDEN] (不拉伸）裁剪模式，通过裁剪的方式保持宽高比
 * [HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_FILL] 视频尺寸进行缩放和拉伸以充满显示视窗
 * [HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_ADAPT] 自适应模式，显示图像和设备的横竖屏不同使用黑边模式，横竖屏相同使用裁剪模式
 */
fun HWRTC.online(context: Context, userId: String): SurfaceView {
    engine.setDefaultSpeakerModel(HRTCSpeakerModel.HRTC_SPEAKER_MODE_SPEAKER)
    return engine.createRenderer(context.applicationContext).apply {
        val ret =
            engine.startRemoteStreamView(userId, this, HRTCStreamType.HRTC_STREAM_TYPE_LD, false)
        if (ret == 0) {
            engine.updateRemoteRenderMode(
                userId,
                HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_FILL,
                HRTCVideoMirrorType.HRTC_VIDEO_MIRROR_TYPE_AUTO
            )
        }
    }
}


//订阅辅流
fun HWRTC.screenShareOnline(context: Context, userId: String): SurfaceView =
    engine.createRenderer(context.applicationContext).apply {
        val ret = engine.startRemoteAuxiliaryStreamView(userId, this)
        if (ret == 0) {
            engine.updateRemoteAuxiliaryStreamRenderMode(
                userId,
                HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_FILL,
                HRTCVideoMirrorType.HRTC_VIDEO_MIRROR_TYPE_AUTO
            )
        }
    }

//远端用户离开房间，并删除远端窗口
fun HWRTC.offline(userId: String) {
    engine.stopRemoteStreamView(userId)
}

//终止远端辅流
fun HWRTC.screenShareOffline(userId: String) {
    engine.stopRemoteAuxiliaryStreamView(userId)
}

fun hmacSha(KEY: String, VALUE: String, SHA_TYPE: String = "HmacSHA256"): String? {
    return try {
        val signingKey = SecretKeySpec(KEY.toByteArray(charset("UTF-8")), SHA_TYPE)
        val mac = Mac.getInstance(SHA_TYPE)
        mac.init(signingKey)
        val rawHmac = mac.doFinal(VALUE.toByteArray(charset("UTF-8")))
        val hexArray = byteArrayOf(
            '0'.code.toByte(), '1'.code.toByte(), '2'.code.toByte(), '3'.code.toByte(),
            '4'.code.toByte(), '5'.code.toByte(), '6'.code.toByte(), '7'.code.toByte(),
            '8'.code.toByte(), '9'.code.toByte(), 'a'.code.toByte(), 'b'.code.toByte(),
            'c'.code.toByte(), 'd'.code.toByte(), 'e'.code.toByte(), 'f'.code.toByte()
        )
        val hexChars = ByteArray(rawHmac.size * 2)
        for (j in rawHmac.indices) {
            val v: Int = rawHmac[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        String(hexChars)
    } catch (ex: Exception) {
        throw RuntimeException(ex)
    }
}