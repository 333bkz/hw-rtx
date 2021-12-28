package com.bkz.chat

import android.util.Log

internal fun String.log(tag: String = "-Chat-") {
    Log.i(tag, this)
}