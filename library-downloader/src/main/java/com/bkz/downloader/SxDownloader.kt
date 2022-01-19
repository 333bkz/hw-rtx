package com.bkz.downloader

import android.content.Context
import com.arialyy.annotations.Download
import com.arialyy.annotations.M3U8.*
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.target.HttpBuilderTarget
import com.arialyy.aria.core.task.DownloadTask


class SxDownloader : IDownloader {

    private val tasks = mutableMapOf<Int, FileTask<Long>>()
    private val calls = mutableListOf<DownloaderListener>()

    companion object {
        @JvmStatic
        val instance: IDownloader by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SxDownloader() }
        var PARENT_FILE: String? = null

        @JvmStatic
        fun init(context: Context) {
            val file = context.appCacheFile("video")
            if (!file.exists()) {
                file.mkdirs()
            }
            PARENT_FILE = file.absolutePath
            Aria.init(context.applicationContext)
        }
    }

    init {
        Aria.download(this).register()
    }

    override fun start(downloadUrl: String, target: FileInfo) {
        var fileTask = tasks[target.fileId]
        if (fileTask == null) {
            val builder = buildTarget(downloadUrl, target)
            val taskId = builder.create() //start
            fileTask = FileTask(target, taskId)
            tasks[target.fileId] = fileTask
        } else {
            val task = Aria.download(this)
                .load(fileTask.task)
            when {
                task.isRunning -> return
                target.fileSuffix == "m3u8" -> {
                    task.m3u8VodOption(createM3U8VodOption())
                        .resume()
                }
                else -> task.resume()
            }
        }
    }

    override fun pause(target: FileInfo) {
        tasks[target.fileId]?.task?.let {
            Aria.download(this).load(it).stop()
        }
    }

    override fun cancel(target: FileInfo) {
        tasks[target.fileId]?.task?.let {
            Aria.download(this).load(it).cancel(true)
        }
    }

    override fun delete(target: FileInfo) {
        cancel(target)
        tasks.remove(target.fileId)
        //fileDao.delete(target)
        //target.filePath?.deleteFile()
    }

    override fun registerProgressListener(listener: DownloaderListener) {
        if (!calls.contains(listener)) {
            calls.add(listener)
        }
    }

    override fun removeProgressListener(listener: DownloaderListener) {
        calls.remove(listener)
    }

    private fun buildTarget(url: String, target: FileInfo): HttpBuilderTarget =
        Aria.download(this)
            .load(url)
            .setFilePath(PARENT_FILE + "/" + target.fileName + "." + target.fileSuffix)
            .apply {
                if ("m3u8" == target.fileSuffix) {
                    m3u8VodOption(createM3U8VodOption())
                }
            }
            .setExtendField(target.fileId.toString())
            .ignoreCheckPermissions()
            .ignoreFilePathOccupy()


    @Download.onWait
    fun onWait(task: DownloadTask) {
        "onWait ${task.entity.str}".log()
    }

    @Download.onPre
    fun onPre(task: DownloadTask) {
        "onPre ${task.entity.str}".log()
    }

    @Download.onTaskStart
    fun taskStart(task: DownloadTask) {
        "taskStart ${task.entity.str}".log()
    }

    @Download.onTaskRunning
    fun running(task: DownloadTask) {
        "running ${task.entity.str} ${task.percent}".log()
    }

    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        "taskResume ${task.entity.str}".log()
    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        "taskStop ${task.entity.str}".log()
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        "taskCancel ${task.entity.str}".log()
    }

    @Download.onTaskFail
    fun taskFail(
        task: DownloadTask,
        e: Exception,
    ) {
        "taskFail ${task.entity.str} ${e.message}".log()
    }

    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        "taskComplete ${task.entity.str} ${task.filePath}".log()
    }

//    @onPeerStart
//    fun onPeerStart(m3u8Url: String, peerPath: String, peerIndex: Int) {
//        "onPeerStart $peerIndex - $m3u8Url - $peerPath".log()
//    }
//
//    @onPeerComplete
//    fun onPeerComplete(m3u8Url: String, peerPath: String, peerIndex: Int) {
//        "onPeerComplete $peerIndex - $m3u8Url - $peerPath".log()
//    }
//
//    @onPeerFail
//    fun onPeerFail(m3u8Url: String, peerPath: String, peerIndex: Int) {
//        "onPeerFail $peerIndex - $m3u8Url - $peerPath".log()
//    }
}