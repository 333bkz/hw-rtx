package com.bkz.downloader

import android.content.Context
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadTask

class SxDownloader : IDownloader {

    var context:Context? = null

    private val listener by lazy {

    }

    private val tasks = mutableMapOf<Int, FileTask<DownloadTask>>()

    private val calls = mutableListOf<DownloaderListener>()

    override fun start(downloadUrl: String, target: FileInfo) {
       val entity = Aria.download(this).getFirstDownloadEntity(downloadUrl)
    }

    override fun pause(target: FileInfo) {
    }

    override fun cancel(target: FileInfo) {
    }

    override fun delete(target: FileInfo) {
    }

    override fun cancelAll() {
    }

    override fun registerProgressListener(listener: DownloaderListener) {
    }

    override fun removeProgressListener(listener: DownloaderListener) {
    }
}