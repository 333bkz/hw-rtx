package com.bkz.demo.ui

import com.bkz.control.MediaControllerListener
import sx.ijk.iPlayer.MediaPlayer

class MediaControllerListenerImpl(
    var mediaPlayer: MediaPlayer? = null
) : MediaControllerListener {

    override fun onSpeed(speed: Float) {

    }

    override fun onSeek(progress: Int) {
        mediaPlayer?.seekTo(progress)
    }

    override fun onDownload() {

    }

    override fun stopPlay() {
        mediaPlayer?.pause()
    }

    override fun startPlay() {
        mediaPlayer?.resume()
    }

    override fun onSwitchPosition(isDragUser: Boolean) {

    }
}