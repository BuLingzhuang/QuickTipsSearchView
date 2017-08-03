package com.bulingzhuang.quicktipssearchview

import android.content.Context
import android.widget.Toast

/**
 * Created by bulingzhuang
 * on 2017/8/1
 * E-mail:bulingzhuang@foxmail.com
 */
object ToastUtil {
    private var mToast: Toast? = null

    public fun showToast(context: Context, string: String, duration: Int = Toast.LENGTH_SHORT) {
        mToast?.let { mToast!!.cancel() }
        mToast = Toast.makeText(context, string, duration)
        mToast!!.show()
    }
}