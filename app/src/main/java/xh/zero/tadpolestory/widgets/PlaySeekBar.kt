package xh.zero.tadpolestory.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import xh.zero.tadpolestory.R

class PlaySeekBar : View {

    companion object {
        private var WRAP_WIDTH = 200
        private var WRAP_HEIGHT = 50
    }

    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRectBackground = RectF()
    private val mRectCache = RectF()
    private val mRectProgress = RectF()
    private var barWidth = 30f
    private val sliderSize = 50
    private val mSlidePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSlideInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mSliderX = 0f

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        var ta: TypedArray? = null
        try {
//            ta = context.theme.obtainStyledAttributes(attrs, R.styleable.RadarView, 0, 0)
//            val color = ta.getColor(R.styleable.RadarView_rv_color, resources.getColor(android.R.color.holo_orange_light))
            mPaint.style = Paint.Style.FILL_AND_STROKE
            mPaint.color = Color.RED

            mSlidePaint.style = Paint.Style.FILL_AND_STROKE
            mSlidePaint.color = resources.getColor(R.color.color_4121FF)
            mSlideInnerPaint.color = Color.WHITE
        } catch (e: Exception) {
            ta?.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WRAP_WIDTH, WRAP_HEIGHT)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WRAP_WIDTH, heightSize)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, WRAP_HEIGHT)
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = (width - paddingLeft - paddingRight).toFloat()
        val height = (height - paddingTop - paddingBottom).toFloat()
        val radius = height / 2
        // 绘制背景
        if (barWidth > height) barWidth = height
        val barPadding = (height - barWidth) / 2
        mRectBackground.set(0f, barPadding, width, barWidth + barPadding)
        canvas.drawRoundRect(mRectBackground, radius, radius, mPaint)
        drawSlider(canvas, radius)
    }

    private fun drawSlider(canvas: Canvas, radius: Float) {
//        val sliderX = mSliderPoint.x
        // 绘制滑块
        canvas.drawCircle(radius, radius, radius, mSlidePaint)
        canvas.drawCircle(radius, radius, radius - 10, mSlideInnerPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mSliderX = event.x
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }
}