package com.bkz.hwrtc

import com.huawei.rtc.models.HRTCStatsInfo
import com.huawei.rtc.utils.HRTCEnums

interface IEventHandler {

    /**
     * 错误回调
     */
    fun onWarning(isError: Boolean, code: Int, msg: String) {}

    /**
     * 签名过期回调，需要app调用renewAuthorization更新签名。
     */
    fun onAuthorizationExpired() {}

    /**
     * 成功加入房间
     */
    fun onJoinRoomSuccess(isRejoin: Boolean) {}

    /**
     * 加入房间失败
     */
    fun onJoinRoomFailure(code: Int, msg: String) {}

    /**
     * 自己离开房间
     */
    fun onLeaveRoom(leaveReason: HRTCEnums.HRTCLeaveReason, statsInfo: HRTCStatsInfo) {}

    /**
     * 远端用户加入房间
     */
    fun onUserOnline(userId: String) {}

    /**
     * 远端用户离线
     */
    fun onUserOffline(userId: String, reason: Int) {}

    /**
     * 屏幕共享流
     */
    fun onScreenShareOnline(userId: String) {}

    /**
     * 屏幕共享流停止
     */
    fun onScreenShareOffline(userId: String) {}
}