package com.bkz.downloader

data class FileInfo(
    var fileId: Int, var fileUrl: String? = null, var courseNo: String = "",
    var fileName: String? = null,
    var filePath: String? = null,
    var fileSuffix: String? = null,
    //下载状态
    @IDownloader.STATUS
    var downloadState: Int = 0,
    //文件类型
    @IDownloader.TYPE
    var fileType: Int = 0,
    //播放记录
    var watchSchedule: Int = 0,
    var duration: Int = 0,
    //百家云下载需要的参数
    var roomId: String? = null,
    var sessionId: Long = 0,
    var token: String? = null,
) {
    var progress: Int = 0
    var decodeUrl: String? = null
    var isSelected: Boolean = false
}