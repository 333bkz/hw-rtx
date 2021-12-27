package com.bkz.control

interface MediaControllerListener {
    fun onSpeed(speed: Float)
    fun onSeek(progress: Int)
    fun onDownload()
    fun stopPlay()
    fun startPlay()
    fun onSwitchPosition(isDragUser: Boolean)
}