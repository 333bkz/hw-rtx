package com.bkz.control

import android.view.View

interface ListPopUpWindow<T> {
    fun dismiss()
    fun show(view: View)
    fun setData(data: List<T>)
    fun setItemSelectedListener(listener: OnItemSelectedListener<T>)
}

interface OnItemSelectedListener<T> {
    fun onItemSelected(value: T)
}