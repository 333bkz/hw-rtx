package com.bkz.downloader

import com.arialyy.aria.core.task.DownloadTask

class SxDownloader : IDownloader {

    private val listener by lazy {

    }

    private val tasks = mutableMapOf<Int, FileTask<DownloadTask>>()

    private val calls = mutableListOf<DownloaderListener>()

    override fun start(downloadUrl: String, target: FileInfo) {
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