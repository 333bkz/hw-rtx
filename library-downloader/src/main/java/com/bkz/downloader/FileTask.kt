package com.bkz.downloader

data class FileTask<T>(
    val fileInfo: FileInfo,
    val task: T,
)