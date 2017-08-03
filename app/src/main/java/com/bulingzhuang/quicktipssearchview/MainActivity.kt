package com.bulingzhuang.quicktipssearchview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        qtv.setOnViewClickListener(object : QuickTipsSearchView.OnViewClickListener {
            override fun onClick(str: String) {
//                baseContext.showToast("点击了：$str")
                ToastUtil.showToast(baseContext, str)
            }
        })
        qtv.setData(arrayListOf("老实人", "小王", "隔壁老王", "隔壁老王的爹", "远州鼠", "落栗", "苏芳", "石竹", "枯草",
                "中间插播一条广告", "过长内容会省略号代替后面部分",
                "柳煤竹茶", "锖青磁", "鸠羽紫", "浅血牙", "元青", "柏坊灰蓝", "胭脂", "Petrol",
                "我", "还是我", "这是第三个我", "没有第四个了", "2017-08-01", "卜令壮"))
    }
}
