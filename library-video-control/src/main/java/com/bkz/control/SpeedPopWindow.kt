package com.bkz.control

import android.view.View

interface SpeedPopUpWindow {

    fun dismiss()
    fun show(view: View)
    fun setData(data: List<String>)
    fun setSpeedItemSelectedListener(listener:OnSpeedItemSelectedListener)
}