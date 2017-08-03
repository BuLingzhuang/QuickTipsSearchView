package com.bulingzhuang.quicktipssearchview

import android.content.Context
import android.widget.Toast

/**
 * Created by bulingzhuang
 * on 2017/8/1
 * E-mail:bulingzhuang@foxmail.com
 */
public fun Context.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, length).show()
}