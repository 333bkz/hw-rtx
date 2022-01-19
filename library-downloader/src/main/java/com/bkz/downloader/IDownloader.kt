package com.bkz.downloader

import androidx.annotation.IntDef

interface IDownloader {

    @IntDef(
        STATUS_DEFAULT,
        STATUS_WAIT,
        STATUS_RUNNING,
        STATUS_PAUSE,
        STATUS_ERROR,
        STATUS_COMPLETED,
    )
    annotation class STATUS

    @IntDef(
        TYPE_MATERIAL,
        TYPE_BAI_JIA,
        TYPE_VIDEO,
        TYPE_SX,
    )
    annotation class TYPE

    companion object {
        const val STATUS_DEFAULT = 0
        const val STATUS_WAIT = 1
        const val STATUS_RUNNING = 2
        const val STATUS_PAUSE = 3
        const val STATUS_ERROR = 4
        const val STATUS_COMPLETED = 5

        const val TYPE_MATERIAL = 0
        const val TYPE_BAI_JIA = 1
        const val TYPE_VIDEO = 2
        const val TYPE_SX = 3
    }

    fun start(downloadUrl: String, target: FileInfo)
    fun pause(target: FileInfo)
    fun cancel(target: FileInfo)
    fun delete(target: FileInfo){}
    fun cancelAll(){}
    fun registerProgressListener(listener: DownloaderListener)
    fun removeProgressListener(listener: DownloaderListener)
}