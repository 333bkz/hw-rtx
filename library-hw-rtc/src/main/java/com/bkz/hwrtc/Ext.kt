package com.bkz.hwrtc

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.huawei.rtc.HRTCEngine
import com.huawei.rtc.models.HRTCJoinParam
import com.huawei.rtc.models.HRTCVideoEncParam
import com.huawei.rtc.utils.HRTCConstants
import com.huawei.rtc.utils.HRTCEnums.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun String.log() {
    Log.i("-HW-RTC-", this)
}

fun videoEncParam() = listOf(
    HRTCVideoEncParam(
        HRTCStreamType.HRTC_STREAM_TYPE_LD,
        160, 90, 24, 15, 270, 64, false
    ),//上行90p 流畅
    HRTCVideoEncParam(
        HRTCStreamType.HRTC_STREAM_TYPE_SD,
        320, 180, 24, 15, 600, 80, false
    ),//上行180p 标清
    HRTCVideoEncParam(
        HRTCStreamType.HRTC_STREAM_TYPE_HD,
        640, 360, 24, 15, 1700, 200, false
    ),//上行360p 高清
    HRTCVideoEncParam(
        HRTCStreamType.HRTC_STREAM_TYPE_FHD,
        1280, 720, 24, 15, 4000, 500, false
    ),//上行720p 全高清
)

//注册监听
fun IEventHandler.registerEventHandler() {
    rtc.handler?.handler?.add(this)
}

//注销监听
fun IEventHandler.unregisterEventHandler() {
    rtc.handler?.handler?.remove(this)
}

//角度
fun Int.rotationType(): HRTCRotationType = when (this) {
    90 -> HRTCRotationType.HRTC_ROTATION_TYPE_90
    180 -> HRTCRotationType.HRTC_ROTATION_TYPE_180
    270 -> HRTCRotationType.HRTC_ROTATION_TYPE_270
    else -> HRTCRotationType.HRTC_ROTATION_TYPE_0
}

//竖向，必须在加入房间前设置屏幕方向
fun HRTCEngine.portrait() {
    setLayoutDirect(HRTCOrientationMode.HRTC_ORIENTATION_MODE_PORTRAIT)
}

//横向，必须在加入房间前设置屏幕方向
fun HRTCEngine.landscape() {
    setLayoutDirect(HRTCOrientationMode.HRTC_ORIENTATION_MODE_LANDSCAPE)
}

//扬声器模式
fun HRTCEngine.defaultSpeakerModel() {
    setDefaultSpeakerModel(HRTCSpeakerModel.HRTC_SPEAKER_MODE_SPEAKER)
}

//接收对应远端用户的音频流
fun HRTCEngine.openRemoteAudio(userId: String) {
    muteRemoteAudio(userId, false)
}

//关闭接收对应远端用户的音频流
fun HRTCEngine.cancelRemoteAudio(userId: String) {
    muteRemoteAudio(userId, true)
}

//加入房间
fun HRTCEngine.joinRoom(
    roomId: String,
    appId: String,
    key: String,
    userId: String,
    userName: String
) = joinRoom(HRTCJoinParam().apply {
    this.userId = userId
    this.userName = userName
    this.role = HRTCJoinParam.HRTCRoleType.HRTC_ROLE_TYPE_PLAYER
    this.roomId = roomId
    this.sfuType = 0
    this.optionalInfo = ""
    this.autoSubscribeAudio = true
    this.autoSubscribeVideo = true
    this.ctime = System.currentTimeMillis() / 1000 + 60 * 60 //有效时间为1小时，单位是秒
    val content: String = (appId + "+"
            + roomId + "+"
            + userId + "+"
            + this.ctime)
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
fun HRTCEngine.online(
    context: Context,
    userId: String,
    streamType: HRTCStreamType = HRTCStreamType.HRTC_STREAM_TYPE_LD,
    displayMode: HRTCVideoDisplayMode = HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_HIDDEN,
): SurfaceView {
    defaultSpeakerModel()
    val surface = createRenderer(context.applicationContext)
    //setRemoteVideoAdjustResolution(true) //下行分辨率自适应默认打开
    val ret: Int = startRemoteStreamView(userId, surface, streamType, false)
    "start user stream result: $ret".log()
    if (ret == 0) {
        updateRemoteRenderMode(
            userId,
            displayMode,
            HRTCVideoMirrorType.HRTC_VIDEO_MIRROR_TYPE_AUTO
        )
    }
    //HRTC_VIDEO_DISPLAY_MODE_FIT : (不拉伸）黑边模式，通过扩边的方式保持宽高比。
    //HRTC_VIDEO_DISPLAY_MODE_HIDDEN : (不拉伸）裁剪模式，通过裁剪的方式保持宽高比。
    //HRTC_VIDEO_DISPLAY_MODE_FILL : 视频尺寸进行缩放和拉伸以充满显示视窗。
    //HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_ADAPT : 自适应模式，显示图像和设备的横竖屏不同使用黑边模式，横竖屏相同使用裁剪模式，只支持Android和iOS平台。
    return surface
}

//订阅辅流
fun HRTCEngine.screenShareOnline(
    context: Context,
    userId: String,
    displayMode: HRTCVideoDisplayMode = HRTCVideoDisplayMode.HRTC_VIDEO_DISPLAY_MODE_HIDDEN,
): SurfaceView {
    val surface = createRenderer(context.applicationContext)
    val ret: Int = startRemoteAuxiliaryStreamView(userId, surface)
    "start aux stream result: $ret".log()
    if (ret == 0) {
        updateRemoteAuxiliaryStreamRenderMode(
            userId,
            displayMode,
            HRTCVideoMirrorType.HRTC_VIDEO_MIRROR_TYPE_AUTO
        )
    }
    return surface
}

//远端用户离开房间，并删除远端窗口
fun HRTCEngine.offline(userId: String) {
    stopRemoteStreamView(userId)
}

//终止远端辅流
fun HRTCEngine.screenShareOffline(userId: String) {
    stopRemoteAuxiliaryStreamView(userId)
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