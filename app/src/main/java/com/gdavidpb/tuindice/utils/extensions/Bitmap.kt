package com.gdavidpb.tuindice.utils.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

fun InputStream.decodeScaledBitmap(scaleFactor: Int): Bitmap {
    val options = BitmapFactory.Options().apply {
        inSampleSize = scaleFactor
    }

    val bitmap = BitmapFactory.decodeStream(this, null, options)

    return bitmap ?: throw RuntimeException("decodeSampledBitmap")
}

fun InputStream.decodeScaleFactor(sample: Int): Int {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    BitmapFactory.decodeStream(this, null, options)

    val width = options.outWidth.toDouble()
    val height = options.outHeight.toDouble()
    val max = max(width, height)

    return (max / sample).roundToInt()
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    return if (degrees != 0f)
        with(Matrix()) {
            postRotate(degrees)

            Bitmap.createBitmap(this@rotate, 0, 0, width, height, this, true)
        }.also { recycle() }
    else
        this
}