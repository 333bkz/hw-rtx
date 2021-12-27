package com.bkz.hwrtc

import com.huawei.rtc.IHRTCEngineEventHandler
import com.huawei.rtc.models.*
import com.huawei.rtc.utils.HRTCEnums

internal class RTCEngineEventHandler : IHRTCEngineEventHandler() {

    internal val handler = mutableListOf<IEventHandler>()

    override fun onWarning(warn: Int, msg: String) {
        handler.forEach {
            it.onWarning(false, warn, msg)
        }
        "onWarning $warn $msg".log()
    }

    override fun onError(error: Int, msg: String) {
        handler.forEach {
            it.onWarning(true, error, msg)
        }
        "onError $error $msg".log()
    }

    override fun onJoinRoomSuccess(roomId: String, userId: String) {
        handler.forEach {
            it.onJoinRoomSuccess(false)
        }
        "onJoinRoomSuccess: $roomId - $userId".log()
    }

    override fun onRejoinRoomSuccess(roomId: String, userId: String) {
        handler.forEach {
            it.onJoinRoomSuccess(true)
        }
        "onRejoinRoomSuccess $userId".log()
    }

    override fun onJoinRoomFailure(error: Int, msg: String) {
        handler.forEach {
            it.onJoinRoomFailure(error, msg)
        }
        "onJoinRoomFailure $error $msg".log()
    }

    override fun onLeaveRoom(reason: HRTCEnums.HRTCLeaveReason, statsInfo: HRTCStatsInfo) {
        handler.forEach {
            it.onLeaveRoom(reason, statsInfo)
        }
        "onLeaveRoom".log()
    }

    override fun onConnectionChangedNotify(
        types: HRTCEnums.HRTCConnStateTypes,
        reason: HRTCEnums.HRTCConnChangeReason,
        desc: String
    ) {
        "onConnectionChangedNotify $types $reason $desc".log()
    }

    override fun onAudioRouteStateChangedNotify(route: HRTCEnums.HRTCAudioRoute) {
        "onAudioRouteStateChangedNotify $route".log()
    }

    override fun onVideoFrameRender(roomId: String?, userId: String?, frame: HRTCVideoFrame?) {
        "onVideoFrameRender $userId".log()
    }

    override fun onRenderExternalVideoFrame(
        roomId: String,
        direction: HRTCEnums.HRTCMediaDirection,
        userId: String,
        videoFrame: HRTCVideoFrame
    ) {
        "onRenderExternalVideoFrame $userId".log()
    }

    override fun onPlaybackExternalAudioFrame(
        roomId: String,
        direction: HRTCEnums.HRTCMediaDirection,
        audioFrame: HRTCAudioFrame
    ) {
        "onPlaybackExternalAudioFrame".log()
    }

    override fun onAuthorizationExpired() {
        handler.forEach {
            it.onAuthorizationExpired()
        }
        "onAuthorizationExpired".log()
    }

    override fun onRemoteAudioStateChangedNotify(
        roomId: String,
        userId: String,
        state: HRTCEnums.HRTCRemoteAudioStreamState,
        reason: HRTCEnums.HRTCRemoteAudioStreamStateReason
    ) {
        "onRemoteAudioStateChangedNotify $state $reason".log()
    }

    override fun onRemoteVideoStateChangedNotify(
        roomId: String,
        userId: String,
        state: HRTCEnums.HRTCRemoteVideoStreamState,
        reason: HRTCEnums.HRTCRemoteVideoStreamStateReason
    ) {
        "onRemoteVideoStateChangedNotify $state $reason".log()
    }

    override fun onLocalAudioStateChangedNotify(
        state: HRTCEnums.HRTCLocalAudioStreamState,
        reason: HRTCEnums.HRTCLocalAudioStreamStateReason
    ) {
        "onLocalAudioStateChangedNotify".log()
    }

    override fun onRemoteUserOnline(roomId: String, userId: String, userName: String) {
        "onRemoteUserOnline $roomId $userId".log()
        handler.forEach {
            it.onUserOnline(userId)
        }
    }

    override fun onRemoteUserOffline(roomId: String, userId: String, reason: Int) {
        "onRemoteUserOffline $roomId $userId $reason".log()
        handler.forEach {
            it.onUserOffline(userId, reason)
        }
    }

    override fun onRemoteUserNameChangedNotify(
        roomId: String?,
        userId: String?,
        userName: String?
    ) {
        "onRemoteUserNameChangedNotify $userId".log()
    }

    override fun onUserNameChangedNotify(oldUserName: String?, newUserName: String?) {
        "onUserNameChangedNotify".log()
    }

    override fun onUserRoleChangedNotify(
        oldRole: HRTCUserInfo.HRTCRoleType?,
        newRole: HRTCUserInfo.HRTCRoleType?
    ) {
        "onUserRoleChangedNotify".log()
    }

    override fun onUserVolumeStatsNotify(
        volumeInfos: MutableList<HRTCVolumeInfo>?,
        totalVolume: Int
    ) {
        "onUserVolumeStatsNotify".log()
    }

    override fun onUserAuxiliaryStreamAvailable(
        roomId: String, userId: String, available: Boolean
    ) {
        "onUserAuxiliaryStreamAvailable $userId $available".log()
        handler.forEach {
            if (available) {
                it.onScreenShareOnline(userId)
            } else {
                it.onScreenShareOffline(userId)
            }
        }
    }

    override fun onAudioFramePlayback(audioFrame: HRTCAudioFrame?) {
        "onAudioFramePlayback".log()
    }

    override fun onLocalVideoStateChangedNotify(
        state: HRTCEnums.HRTCLocalVideoStreamState?,
        reason: HRTCEnums.HRTCLocalVideoStreamStateReason?
    ) {
        "onLocalVideoStateChangedNotify".log()
    }

    override fun onNetworkTestQuality(level: HRTCEnums.HRTCNetworkQualityLevel?) {
        "onNetworkTestQuality".log()
    }

    override fun onNetworkTestResult(networkTestResult: HRTCNetworkTestResult?) {
        "onNetworkTestResult".log()
    }

    override fun onHowlingUpDetected(result: Int) {
        "onHowlingUpDetected".log()
    }

    override fun onHowlingDownDetected(result: Int) {
        "onHowlingDownDetected".log()
    }

    override fun onMediaStreamRecvPktNotify(streamPacketInfo: MutableList<HRTCStreamPacketInfo>?) {
        "onMediaStreamRecvPktNotify".log()
    }

    override fun onRenderSuccessNotify(userId: String?, isAux: Boolean) {
        "onRenderSuccessNotify $userId $isAux".log()
    }

    override fun onScreenShareStarted() {
        "onScreenShareStarted".log()
    }

    override fun onScreenShareStopped(reason: Int) {
        "onScreenShareStopped".log()
    }

    override fun onMediaConnectStateChangedNotify(
        state: HRTCEnums.HRTCMediaConnStateTypes?,
        reason: HRTCEnums.HRTCMediaConnChangeReason?,
        description: String?
    ) {
        "onMediaConnectStateChangedNotify $description".log()
    }

    override fun onAudioMixStateChangedNotify(
        state: HRTCEnums.HRTCAudioFileState?,
        reason: HRTCEnums.HRTCAudioFileReason?,
        value: Long
    ) {
        "onAudioMixStateChangedNotify".log()
    }

    override fun onAudioClipFinished(soundId: Int) {
        "onAudioClipFinished".log()
    }

    override fun onLocalVideoStatsNotify(localStats: MutableList<HRTCLocalVideoStats>?) {
        "onLocalVideoStatsNotify".log()
    }

    override fun onLocalAudioStatsNotify(localStats: MutableList<HRTCLocalAudioStats>?) {
        "onLocalAudioStatsNotify".log()
    }

    override fun onVideoResolutionChangedNotify(userId: String?, width: Int, height: Int) {
        "onVideoResolutionChangedNotify".log()
    }

    override fun onLocalVolumeChangedNotify(volume: Int, muted: Int) {
        "onLocalVolumeChangedNotify".log()
    }

    override fun onSeiSendMsgSuccess(message: String?) {
        "onSeiSendMsgSuccess".log()
    }

    override fun onSeiRecvMsg(userId: String?, message: String?) {
        "onSeiRecvMsg $userId".log()
    }

    override fun onStartPublishStream(code: Int, taskId: String?) {
        "onStartPublishStream".log()
    }

    override fun onUpdateTransCoding(code: Int, taskId: String?) {
        "onUpdateTransCoding".log()
    }

    override fun onStopPublishStream(code: Int, taskId: String?) {
        "onStopPublishStream".log()
    }

    override fun onStreamPublishStateChange(
        code: Int,
        taskId: String?,
        urlStatusList: MutableList<HRTCRtmpUrlInfo>?
    ) {
        "onStreamPublishStateChange".log()
    }
}