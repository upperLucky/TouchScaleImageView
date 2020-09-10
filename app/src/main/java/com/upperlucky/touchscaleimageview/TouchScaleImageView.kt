package com.upperlucky.touchscaleimageview

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
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

class TouchScaleImageView(context: Context?, attrs: AttributeSet?) : View(context, attrs){

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmap = getAvatar(resources, IMAGE_SIZE.toInt())
    private var smallScale = 0f
    private var bigScale = 0f
    private var originalOffsetX = 0f
    private var originalOffsetY = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var big = false
    private val gestureListener = GestureListener()
    private val scaleGestureListener = ScaleGestureListener()
    private val flingRunnable = FlingRunnable()
    private var currentScale = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var scaleAnimator = ObjectAnimator.ofFloat(this,"currentScale",0f,1f)


    private val gestureDecoder = GestureDetectorCompat(context, gestureListener)
    private val scaleGestureDetector = ScaleGestureDetector(context,scaleGestureListener)

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
        val scaleFraction = (currentScale - smallScale) / (bigScale - smallScale)
        canvas.translate(offsetX * scaleFraction,offsetY * scaleFraction)
        canvas.scale(currentScale, currentScale, width / 2f, height / 2f)
        canvas.drawBitmap(bitmap, originalOffsetX, originalOffsetY, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            gestureDecoder.onTouchEvent(event)
        }
        return true
//        return gestureDecoder.onTouchEvent(event)
//        return scaleGestureDetector.onTouchEvent(event)
    }


    private fun fixOffset() {
        offsetX = min(offsetX, ((bitmap.width * bigScale - width) / 2))
        offsetX = max(offsetX, -((bitmap.width * bigScale - width) / 2))
        offsetY = min(offsetY, ((bitmap.height * bigScale - height) / 2))
        offsetY = max(offsetY, -((bitmap.height * bigScale - height) / 2))
    }


    inner class GestureListener : GestureDetector.SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onScroll(
            startEvent: MotionEvent?,
            currentEvent: MotionEvent?,
            distanceX: Float, // 这个距离是起点的坐标减去终点的距离的横坐标
            distanceY: Float // 这个距离是起点的坐标减去终点的距离的纵坐标
        ): Boolean {
            if (big) {
                offsetX -= distanceX
                offsetY -= distanceY
                fixOffset()
                invalidate()
            }
            return false
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
                ViewCompat.postOnAnimation(this@TouchScaleImageView,flingRunnable)
            }
            return false
        }


        override fun onDoubleTap(e: MotionEvent): Boolean {
            big = !big
            if (big) {
                offsetX = (e.x - width / 2f) * (1 - bigScale / smallScale)
                offsetY = (e.x - height / 2f) * (1 - bigScale / smallScale)
                fixOffset()
                scaleAnimator.start()
            } else {
                scaleAnimator.reverse()
            }

            return true
        }
    }

    inner class ScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener{
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // 返回值返回true的时候表示detector.scaleFactor是这一次和上一次的比值
            // 返回false的时候表示detector.scaleFactor是这一次和第一次的比值
            val tempScale = currentScale * detector.scaleFactor
            if (tempScale < smallScale || tempScale > bigScale) {
                return false
            } else {
                currentScale *= detector.scaleFactor
                return true
            }
//            currentScale.coerceAtLeast(smallScale).coerceAtMost(bigScale)
//            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            // 初始值
            offsetX = (detector.focusX - width / 2f) * (1 - bigScale / smallScale)
            offsetY = (detector.focusY - height / 2f) * (1 - bigScale / smallScale)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {

        }
    }

    inner class FlingRunnable : Runnable{
        override fun run() {
            if (scroll.computeScrollOffset()) {
                offsetX = scroll.currX.toFloat()
                offsetY = scroll.currY.toFloat()
                ViewCompat.postOnAnimation(this@TouchScaleImageView,this)
            }
        }
    }

}