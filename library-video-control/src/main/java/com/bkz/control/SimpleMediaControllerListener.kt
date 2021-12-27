package com.bkz.control

abstract class SimpleMediaControllerListener : MediaControllerListener {
    override fun onSpeed(speed: Float) {}
    override fun onSeek(progress: Int) {}
    override fun onDownload() {}
    override fun stopPlay() {}
    override fun startPlay() {}
    override fun onSwitchPosition(isDragUser: Boolean) {}
}