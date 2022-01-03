package com.bkz.control

interface MediaControllerListener {
    fun onSpeed(speed: Float)
    fun onSeek(progress: Int)
    fun onDownload()
    fun stopPlay()
    fun startPlay()
    fun onSwitchPosition(isDragUser: Boolean)
}

abstract class SimpleMediaControllerListener : MediaControllerListener {
    override fun onSpeed(speed: Float) {}
    override fun onSeek(progress: Int) {}
    override fun onDownload() {}
    override fun stopPlay() {}
    override fun startPlay() {}
    override fun onSwitchPosition(isDragUser: Boolean) {}
}