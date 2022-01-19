package com.bkz.downloader

import android.content.Context
import android.net.Uri
import android.util.Log
import com.arialyy.aria.core.download.M3U8Entity
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.processor.IBandWidthUrlConverter
import com.arialyy.aria.core.processor.IKeyUrlConverter
import com.arialyy.aria.core.processor.ITsMergeHandler
import com.arialyy.aria.core.processor.IVodTsUrlConverter
import java.io.File

internal fun createM3U8VodOption() = M3U8VodOption().apply {
    //setBandWidth(200000);
    //generateIndexFile()
    //merge(true)
    //setVodTsUrlConvert(VodTsUrlConverter());
    //setMergeHandler(TsMergeHandler());
    //setKeyUrlConverter(KeyUrlConverter());
    setUseDefConvert(true);
    generateIndexFile();
    setBandWidthUrlConverter(BandWidthUrlConverter());
}

internal class VodTsUrlConverter : IVodTsUrlConverter {
    override fun convert(m3u8Url: String, tsUrls: List<String>): List<String> {
        val uri: Uri = Uri.parse(m3u8Url)
        return tsUrls
    }
}

internal class TsMergeHandler : ITsMergeHandler {
    override fun merge(m3U8Entity: M3U8Entity?, tsPath: List<String>): Boolean {
        return false
    }
}

internal class BandWidthUrlConverter : IBandWidthUrlConverter {
    override fun convert(m3u8Url: String, bandWidthUrl: String): String {
        val index = m3u8Url.lastIndexOf("/")
        "convert $m3u8Url $bandWidthUrl".log()
        return m3u8Url.substring(0, index + 1) + bandWidthUrl
    }
}

internal class KeyUrlConverter : IKeyUrlConverter {
    override fun convert(m3u8Url: String, tsListUrl: String, keyUrl: String): String? {
        return null
    }
}

fun String.log(tag: String = "-Downloader-") {
    Log.i(tag, this)
}

/**
 * app缓存目录
 */
fun Context.appCacheFile(dir: String): File = File(externalCacheDir, dir)