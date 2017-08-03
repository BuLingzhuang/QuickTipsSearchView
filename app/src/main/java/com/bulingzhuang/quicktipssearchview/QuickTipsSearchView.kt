package com.bulingzhuang.quicktipssearchview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Created by bulingzhuang
 * on 2017/7/31
 * E-mail:bulingzhuang@foxmail.com
 */
class QuickTipsSearchView : View {

    //是否使用缓存
    private var mUseCache: Boolean = false
    //缓存画布
    private val mCacheCanvas: Canvas = Canvas()
    //缓存Bitmap
    private lateinit var mCacheBitmap: Bitmap
    //缓存用画笔
    private val mCachePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //背景画笔
    private val mBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //背景矩形
    private lateinit var mBgRect: Rect
    //文字背景圆角矩形RectF
    private val mTextBgRectF = RectF()
    //文字画笔
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //文字高度
    private var mTextYOffset: Float = 0.0f
    //数据
    private var mDataList: ArrayList<TipsEntity> = ArrayList()
    //当前设备密度
    private var mDensity: Float = resources.displayMetrics.density
    //item间隔
    private var mMarginX: Float = 0.0f
    private var mMarginY: Float = 0.0f
    //item文字边距
    private var mPaddingX: Float = 0.0f
    private var mPaddingY: Float = 0.0f
    //item文字圆角半径
    private var mTextBgRadius: Float = 0.0f
    //上一次x、y所在位置
    private var mLastX: Float = 0.0f
    private var mLastY: Float = 0.0f

    private var mOnClickListener: OnViewClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * 初始化
     */
    init {
        mMarginX = mDensity * 12
        mMarginY = mDensity * 8
        mPaddingY = mDensity * 8
        mPaddingX = mDensity * 12
        mTextBgRadius = mDensity * 6 * 0.7f
        mTextPaint.textSize = mDensity * 14
        mTextPaint.color = Color.WHITE
        val fontMetrics = mTextPaint.fontMetrics
        mTextYOffset = -fontMetrics.ascent - fontMetrics.descent
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val max = Math.max(w, h).toFloat()
        mBgRect = Rect(0, 0, w, h)
        mCacheBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCacheCanvas.setBitmap(mCacheBitmap)

        val colors = intArrayOf(Color.parseColor("#e51c23"), Color.parseColor("#e91e63"), Color.parseColor("#9c27b0"),
                Color.parseColor("#673ab7"), Color.parseColor("#3f51b5"), Color.parseColor("#5677fc"),
                Color.parseColor("#03a9f4"), Color.parseColor("#00bcd4"), Color.parseColor("#009688"),
                Color.parseColor("#259b24"), Color.parseColor("#8bc34a"), Color.parseColor("#cddc39"),
                Color.parseColor("#ffeb3b"), Color.parseColor("#ffc107"), Color.parseColor("#ff9800"),
                Color.parseColor("#ff5722"))
        val marginF = 1f / (colors.size - 1)
        val positions = floatArrayOf(0f, marginF * 1, marginF * 2, marginF * 3, marginF * 4, marginF * 5, marginF * 6, marginF * 7, marginF * 8, marginF * 9, marginF * 10, marginF * 11, marginF * 12, marginF * 13, marginF * 14, 1f)
        val gradient = LinearGradient(0f, 0f, max, max, colors, positions, Shader.TileMode.CLAMP)

        mBgPaint.shader = gradient
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mUseCache) {//使用缓存数据，绘制单条删除状态
            Log.e("blz", "使用缓存绘制")
            canvas?.drawBitmap(mCacheBitmap, 0f, 0f, mCachePaint)
        } else {//初始化和数据改变以后，重新绘制缓存内容
            Log.e("blz", "绘制标签内容")
            mLastX = 0.0f
            mLastY = 0.0f

            //画文字
            mDataList.forEach {
                val availableX = width - mLastX
                val itemX = mMarginX + mPaddingX * 2 + mTextPaint.measureText(it.content)
                //当前条目宽度大于可用宽度，需要换行
                if (itemX > availableX) {
                    mLastX = 0.0f
                    mLastY += mMarginY + mPaddingY * 2 + mTextYOffset
                }
                it.left = mLastX + mMarginX
                it.top = mLastY + mMarginY
                it.right = mLastX + itemX
                it.bottom = mLastY + mMarginY + mPaddingY * 2 + mTextYOffset
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mCacheCanvas.drawRoundRect(it.left, it.top, it.right, it.bottom, mTextBgRadius, mTextBgRadius, mBgPaint)
                } else {
                    mTextBgRectF.set(it.left, it.top, it.right, it.bottom)
                    mCacheCanvas.drawRoundRect(mTextBgRectF, mTextBgRadius, mTextBgRadius, mBgPaint)
                }
                mCacheCanvas.drawText(it.content, mLastX + mMarginX + mPaddingX, mLastY + mMarginY + mPaddingY + mTextYOffset, mTextPaint)
                mLastX += itemX
            }

            //把缓存图片绘制到主画布上
            canvas?.drawBitmap(mCacheBitmap, 0f, 0f, mCachePaint)
        }
        mUseCache = true
    }

    interface OnViewClickListener {
        fun onClick(str: String)
    }

    fun setOnViewClickListener(listener: OnViewClickListener) {
        mOnClickListener = listener
    }

    private var mDownX: Float = 0.0f
    private var mDownY: Float = 0.0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDownX = x
                    mDownY = y

                    Log.e("blz", "Touch状态：DOWN，x=$mDownX，y=$mDownY")
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.e("blz", "Touch状态：MOVE，x=$mDownX，y=$mDownY")
                }
                MotionEvent.ACTION_HOVER_MOVE -> {
                    Log.e("blz", "Touch状态：HOVER_MOVE，x=$mDownX，y=$mDownY")
                }
                MotionEvent.ACTION_UP -> {
                    Log.e("blz", "Touch状态：UP，x=$mDownX，y=$mDownY")
                    val absX = Math.abs(x - mDownX)
                    val absY = Math.abs(y - mDownY)
                    if (absX < 3 * mDensity && absY < 3 * mDensity) {
                        Log.e("blz", "是点击事件，触发")
                        mDataList.filter { it.left <= mDownX && it.right >= mDownX && it.top <= mDownY && it.bottom >= mDownY }
                                .forEach {
                                    it.realContent?.let { it1 -> mOnClickListener?.onClick(it1) }
                                }
                    }
                }
                else ->{
                    Log.e("blz", "Touch状态：其他状态(${event.action})，x=$mDownX，y=$mDownY")
                }
            }
        }
        return true
    }

    /**
     * 传入数据
     */
    fun setData(dataList: ArrayList<String>) {
        mapDataList(dataList)
        invalidate()
    }

    /**
     * 处理数据，对超长字符串截取
     */
    private fun mapDataList(data: ArrayList<String>) {
        mDataList.clear()
        //看起来很酷
        data.forEach {
            if (it.length > 6) {
                val str = it.substring(0, 6) + "…"
                mDataList.add(TipsEntity(str, it))
            } else {
                mDataList.add(TipsEntity(it, it))
            }
        }
    }

    private class TipsEntity(var content: String?, var realContent: String?, var left: Float = 0.0f, var top: Float = 0.0f, var right: Float = 0.0f, var bottom: Float = 0.0f)
}