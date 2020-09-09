package com.upperlucky.touchscaleimageview

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import kotlin.math.max
import kotlin.math.min

/**
 * created by yunKun.wen on 2020/9/9
 * desc:
 */

private val IMAGE_SIZE = 300.dp
private const val EXTRA_SCALE_FACOTR = 1.5f
private val OVER_SCROLL = 30.dp

class TouchScaleImageView(context: Context?, attrs: AttributeSet?) : View(context, attrs),
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, Runnable {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmap = getAvatar(resources, IMAGE_SIZE.toInt())
    private var smallScale = 0f
    private var bigScale = 0f
    private var currentScale = 0f
    private var originalOffsetX = 0f
    private var originalOffsetY = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var big = false
    private var scaleFraction = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var scaleAnimator = ObjectAnimator.ofFloat(this,"scaleFraction",0f,1f)

    private val gestureDecoder = GestureDetectorCompat(context, this).apply {
        setOnDoubleTapListener(this@TouchScaleImageView)
    }

    private val scroll = OverScroller(context)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        originalOffsetX = (width - IMAGE_SIZE) / 2f
        originalOffsetY = (height - IMAGE_SIZE) / 2f

        if (bitmap.width / bitmap.height.toFloat() > width / height.toFloat()) {
            smallScale = width / bitmap.width.toFloat()
            bigScale = height / bitmap.height.toFloat() * EXTRA_SCALE_FACOTR
        } else {
            smallScale = height / bitmap.height.toFloat()
            bigScale = width / bitmap.width.toFloat() * EXTRA_SCALE_FACOTR
        }
        currentScale = smallScale
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(offsetX,offsetY)
        val scale = smallScale + (bigScale - smallScale) * scaleFraction
        canvas.scale(scale, scale, width / 2f, height / 2f)
        canvas.drawBitmap(bitmap, originalOffsetX, originalOffsetY, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDecoder.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {

    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        startEvent: MotionEvent?,
        currentEvent: MotionEvent?,
        distanceX: Float, // 这个距离是起点的坐标减去终点的距离的横坐标
        distanceY: Float // 这个距离是起点的坐标减去终点的距离的纵坐标
    ): Boolean {
        if (big) {
            offsetX -= distanceX
            offsetX = min(offsetX, ((bitmap.width * bigScale - width) / 2))
            offsetX = max(offsetX, -((bitmap.width * bigScale - width) / 2))
            offsetY -= distanceY
            offsetY = min(offsetY, ((bitmap.height * bigScale - height) / 2))
            offsetY = max(offsetY, -((bitmap.height * bigScale - height) / 2))
            invalidate()
        }
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (big) {
            scroll.fling(offsetX.toInt(),offsetY.toInt(),velocityX.toInt(),velocityY.toInt(),
                (-(bitmap.width * bigScale - width) / 2).toInt(),
                ((bitmap.width * bigScale - width) / 2).toInt(),
                (-(bitmap.height * bigScale - height) / 2).toInt(),
                ((bitmap.height * bigScale - height) / 2).toInt())
            ViewCompat.postOnAnimation(this,this)
        }
        return false
    }

    override fun run() {
        if (scroll.computeScrollOffset()) {
            offsetX = scroll.currX.toFloat()
            offsetY = scroll.currY.toFloat()
            ViewCompat.postOnAnimation(this,this)
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        big = !big
        if (big) {
            scaleAnimator.start()
        } else {
            scaleAnimator.reverse()
        }

        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }


}