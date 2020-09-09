package com.upperlucky.touchscaleimageview

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.TypedValue

/**
 * created by yunKun.wen on 2020/9/9
 * desc:
 */

val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.dp
    get() = this.toFloat().dp

fun getAvatar(resources: Resources,imageSize: Int): Bitmap {
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(resources,R.drawable.avatar_rengwuxian,options)
    options.inJustDecodeBounds = false
    options.inDensity = options.outWidth
    options.inTargetDensity = imageSize
    return BitmapFactory.decodeResource(resources,R.drawable.avatar_rengwuxian,options)
}