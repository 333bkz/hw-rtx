package com.bkz.hwrtc

import com.huawei.rtc.IHRTCEngineEventHandler
import com.huawei.rtc.models.HRTCStatsInfo
import com.huawei.rtc.utils.HRTCEnums

internal class RTCEngineEventHandler : IHRTCEngineEventHandler() {

    internal var handler: IEventHandler? = null

    override fun onWarning(warn: Int, msg: String) {
        handler?.onWarning(false, warn, msg)
    }

    override fun onError(error: Int, msg: String) {
        handler?.onWarning(true, error, msg)
    }

    override fun onJoinRoomSuccess(roomId: String, userId: String) {
        handler?.onJoinRoomSuccess(false)
    }

    override fun onRejoinRoomSuccess(roomId: String, userId: String) {
        handler?.onJoinRoomSuccess(true)
    }

    override fun onJoinRoomFailure(error: Int, msg: String) {
        handler?.onJoinRoomFailure(error, msg)
    }

    override fun onLeaveRoom(reason: HRTCEnums.HRTCLeaveReason, statsInfo: HRTCStatsInfo) {
        handler?.onLeaveRoom(reason.name)
    }

    override fun onAuthorizationExpired() {
        handler?.onAuthorizationExpired()
    }

    override fun onRemoteUserOnline(roomId: String, userId: String, userName: String) {
        handler?.onUserOnline(userId)
    }

    override fun onRemoteUserOffline(roomId: String, userId: String, reason: Int) {
        handler?.onUserOffline(userId, reason)
    }

    override fun onUserAuxiliaryStreamAvailable(
        roomId: String, userId: String, available: Boolean
    ) {
        if (available) {
            handler?.onScreenShareOnline(userId)
        } else {
            handler?.onScreenShareOffline(userId)
        }
    }

    override fun onFirstRemoteAuxiliaryStreamDecoded(
        roomId: String?, userId: String?, width: Int, height: Int, elapsed: Int
    ) {
        handler?.onScreenSize(width, height)
    }

    override fun onFirstRemoteVideoDecoded(
        roomId: String?, userId: String?, width: Int, height: Int, elapsed: Int
    ) {
        handler?.onUserSize(width, height)
    }
}