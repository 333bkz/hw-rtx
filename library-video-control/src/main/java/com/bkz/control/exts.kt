package com.bkz.control

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visibleOrInvisible(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

fun View.visibleOrGone(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun Context.dp2px(dp: Int): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

fun Context.px2sp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

fun Context.px2dp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

var lastClickTime = 0L

inline fun noFastClick(target: Long = 1000, crossinline func: () -> Unit) {
    val currentTime = System.currentTimeMillis()
    val interval = currentTime - lastClickTime
    if (lastClickTime != 0L && (interval < target)) return
    lastClickTime = currentTime
    func()
}

inline fun View.onClick(interval: Long = 500, crossinline action: (view: View) -> Unit) {
    setOnClickListener { v ->
        noFastClick(interval) {
            action(v)
        }
    }
}

fun Long.toTimeSlot(): String {
    val time = this / 1000
    return String.format("%02d", time / 3600) + ":" + String.format("%02d",
        time % 3600 / 60) + ":" + String.format("%02d", time % 3600 % 60)
}

val Context.statusBarHeight
    get() = run {
        var result = 0
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        result
    }

val Context.screenWidth
    get() = resources.displayMetrics.widthPixels

val Context.screenHeight
    get() = resources.displayMetrics.heightPixels

@SuppressLint("RestrictedApi")
fun AppCompatActivity.hideSupportActionBar(actionBar: Boolean = true, statusBar: Boolean = true) {
    if (actionBar) {
        supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            hide()
        }
    }
    if (statusBar) {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}

@SuppressLint("RestrictedApi")
fun AppCompatActivity.showSupportActionBar(actionBar: Boolean = true, statusBar: Boolean = true) {
    if (actionBar) {
        supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            show()
        }
    }
    if (statusBar) {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}

fun Activity.hideNavKey() {
    if (Build.VERSION.SDK_INT >= 29) { //设置屏幕始终在前面，不然点击鼠标，重新出现虚拟按键
        window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
    } else { //设置屏幕始终在前面，不然点击鼠标，重新出现虚拟按键
        window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
    }
}

fun Activity.showNavKey(systemUiVisibility: Int) {
    window?.decorView?.systemUiVisibility = systemUiVisibility
}

fun View?.isShouldHideInput(event: MotionEvent): Boolean {
    if (this is EditText) {
        val rect = Rect()
        getGlobalVisibleRect(rect)
        return !rect.contains(event.rawX.toInt(), event.rawY.toInt())
    }
    return false
}

fun Activity.hideSoftKeyboard() {
    val view = currentFocus
    view?.let {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}