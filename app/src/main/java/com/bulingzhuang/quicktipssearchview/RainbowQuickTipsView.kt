package com.bulingzhuang.quicktipssearchview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

/**
 * Created by bulingzhuang
 * on 2017/7/31
 * E-mail:bulingzhuang@foxmail.com
 */
class RainbowQuickTipsView : View {

    //是否使用缓存
    private var mUseCache: Boolean = false
    //缓存画布
    private val mCacheCanvas: Canvas = Canvas()
    //缓存Bitmap
    private lateinit var mCacheBitmap: Bitmap
    //缓存用画笔
    private val mCachePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //删除画笔
    private val mDelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //删除叉号画笔
    private val mDelClosePaint = Paint(Paint.ANTI_ALIAS_FLAG)
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
        mDelPaint.color = Color.parseColor("#EA0C25")
        mDelClosePaint.color = Color.WHITE
        mDelClosePaint.strokeWidth = mDensity * 3
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
            canvas?.drawBitmap(mCacheBitmap, 0f, 0f, mCachePaint)
            if (mCheckTipsEntity != null && mDownX - mMoveX > 1) {
//                val width = Math.max(mMoveX, mCheckTipsEntity!!.width)
                val width = Math.min(mDownX - mMoveX, mCheckTipsEntity!!.right - mCheckTipsEntity!!.left)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    canvas?.drawRoundRect(mCheckTipsEntity!!.right - width, mCheckTipsEntity!!.top, mCheckTipsEntity!!.right, mCheckTipsEntity!!.bottom, mTextBgRadius, mTextBgRadius, mDelPaint)
                } else {
                    mTextBgRectF.set(mCheckTipsEntity!!.right - width, mCheckTipsEntity!!.top, mCheckTipsEntity!!.right, mCheckTipsEntity!!.bottom)
                    canvas?.drawRoundRect(mTextBgRectF, mTextBgRadius, mTextBgRadius, mDelPaint)
                }
                if (width > mTextYOffset * 2.33) {
                    //删除显示叉号
                    val closeWidth = mCheckTipsEntity!!.right - mCheckTipsEntity!!.left - width
                    canvas?.drawLine((width - mTextYOffset) / 2 + mCheckTipsEntity!!.left + closeWidth,
                            (mCheckTipsEntity!!.bottom - mCheckTipsEntity!!.top - mTextYOffset) / 2 + mCheckTipsEntity!!.top,
                            (width + mTextYOffset) / 2 + mCheckTipsEntity!!.left + closeWidth,
                            (mCheckTipsEntity!!.bottom - mCheckTipsEntity!!.top + mTextYOffset) / 2 + mCheckTipsEntity!!.top,
                            mDelClosePaint)
                    canvas?.drawLine((width + mTextYOffset) / 2 + mCheckTipsEntity!!.left + closeWidth,
                            (mCheckTipsEntity!!.bottom - mCheckTipsEntity!!.top - mTextYOffset) / 2 + mCheckTipsEntity!!.top,
                            (width - mTextYOffset) / 2 + mCheckTipsEntity!!.left + closeWidth,
                            (mCheckTipsEntity!!.bottom - mCheckTipsEntity!!.top + mTextYOffset) / 2 + mCheckTipsEntity!!.top,
                            mDelClosePaint)
                }
            }
        } else {//初始化和数据改变以后，重新绘制缓存内容
            Log.e("blz", "绘制标签内容")
            mLastX = 0.0f
            mLastY = 0.0f
            mCacheCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR)
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

    //按下的时候记录的x、y值
    private var mDownX: Float = 0.0f
    private var mDownY: Float = 0.0f
    private var mMoveX: Float = 0.0f
    private var mMoveY: Float = 0.0f
    //侧滑选中的条目
    private var mCheckTipsEntity: TipsEntity? = null
    //侧滑成功标记
    private var mHasCheck: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDownX = x
                    mDownY = y
                    mMoveX = x
                    mMoveY = y
                    Log.e("blz", "Touch状态：DOWN，x=$x，y=$y")
//                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    mMoveX = x
                    mMoveY = y
                    Log.e("blz", "Touch状态：MOVE，x=$x，y=$y")
                    if (mMoveX - mDownX < -17 * mDensity && mCheckTipsEntity == null) {
                        Log.e("blz", "开始触发侧滑事件")
                        mDataList.filter { it.left <= mDownX && it.right >= mDownX && it.top <= mDownY && it.bottom >= mDownY }
                                .forEach {
                                    mCheckTipsEntity = it
                                }
                    }
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    Log.e("blz", "Touch状态：UP，x=$x，y=$y")

                    val absX = Math.abs(x - mDownX)
                    val absY = Math.abs(y - mDownY)
                    if (absX < 7 * mDensity && absY < 7 * mDensity) {
                        Log.e("blz", "触发点击事件")
                        if (mHasCheck && mCheckTipsEntity != null) {
                            if (mCheckTipsEntity!!.left <= mDownX && mCheckTipsEntity!!.right >= mDownX
                                    && mCheckTipsEntity!!.top <= mDownY && mCheckTipsEntity!!.bottom >= mDownY) {
                                mDataList.remove(mCheckTipsEntity!!)
                                mUseCache = false
//                                invalidate()
                                Toast.makeText(context, "删除：${mCheckTipsEntity!!.realContent}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            mDataList.filter { it.left <= mDownX && it.right >= mDownX && it.top <= mDownY && it.bottom >= mDownY }
                                    .forEach {
                                        it.realContent?.let { it1 -> mOnClickListener?.onClick(it1) }
                                    }
                        }
                        mCheckTipsEntity = null
                        mHasCheck = false
                    } else if (mCheckTipsEntity != null) {
                        if (mDownX - x >= (mCheckTipsEntity!!.right - mCheckTipsEntity!!.left) / 2) {
                            //侧滑过半，触发侧滑成功状态
                            mHasCheck = true
                            mMoveX = mDownX - (mCheckTipsEntity!!.right - mCheckTipsEntity!!.left)
//                            invalidate()
                        } else if (mDownX - x < (mCheckTipsEntity!!.right - mCheckTipsEntity!!.left) / 2 && mDownX - x >= 0) {
                            mMoveX = mDownX
                            mMoveY = mDownY
                            mCheckTipsEntity = null
                            mHasCheck = false
//                            invalidate()
                        }
                    } else {
                        mCheckTipsEntity = null
                        mHasCheck = false
                    }
                    invalidate()
                }
                else -> {
                    Log.e("blz", "Touch状态：其他状态(${event.action})，x=$x，y=$y")
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
            } else if (it.length == 1) {
                mDataList.add(TipsEntity("    $it    ", it))
            } else if (it.length == 2) {
                mDataList.add(TipsEntity("  $it  ", it))
            } else {
                mDataList.add(TipsEntity(it, it))
            }
        }
    }

    private class TipsEntity(var content: String?, var realContent: String?, var left: Float = 0.0f, var top: Float = 0.0f, var right: Float = 0.0f, var bottom: Float = 0.0f)
}