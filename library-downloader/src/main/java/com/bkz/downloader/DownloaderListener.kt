package com.bkz.downloader


interface DownloaderListener {
    fun onWait(fileInfo: FileInfo)
    fun onStart(fileInfo: FileInfo)
    fun onCompleted(fileInfo: FileInfo)
    fun onPause(fileInfo: FileInfo)
    fun onError(fileInfo: FileInfo)
    fun onCancel(fileInfo: FileInfo)
    fun onProgress(fileInfo: FileInfo, progress: Int)
}