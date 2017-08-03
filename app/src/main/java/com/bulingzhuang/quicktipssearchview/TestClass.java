package com.bulingzhuang.quicktipssearchview;

import android.content.Context;
import android.graphics.Paint;
import android.widget.Toast;

/**
 * Created by bulingzhuang
 * on 2017/7/31
 * E-mail:bulingzhuang@foxmail.com
 */

public class TestClass {
    private static Toast mToast;

    public static void toast(Context context, String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
