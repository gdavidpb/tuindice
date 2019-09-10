package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.gdavidpb.tuindice.R
import kotlin.math.pow
import kotlin.math.sqrt

class CustomSeekBar(context: Context, attrs: AttributeSet)
    : AppCompatSeekBar(context, attrs) {

    companion object {
        private var paint = Paint()
        private var color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        /* Trick to extract seek bar background color */
        if (color == Color.WHITE) {
            val bitmapHook = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvasHook = Canvas(bitmapHook)

            canvasHook.drawColor(Color.WHITE)

            super.onDraw(canvasHook)

            val x = (width - paddingRight - resources.getDimension(R.dimen.size_tick).toInt() - 1)

            color = (0 until height)
                    .map { y -> bitmapHook.getPixel(x, y) }
                    .distinct()
                    .maxBy { target -> target distanceTo color } ?: color

            paint.color = color
        }

        super.onDraw(canvas)

        if (progress < max) {
            val width = (width - (paddingLeft + paddingRight)).toFloat()
            val radius = (resources.getDimension(R.dimen.size_tick) / 2f) + 0.1f
            val step = (width / max)

            val translateX = paddingLeft.toFloat()
            val halfHeight = (height / 2f)

            for (i in (progress + 1)..max)
                canvas.drawCircle((i * step) + translateX, halfHeight, radius, paint)
        }
    }

    private infix fun Int.distanceTo(x: Int): Double {
        val (a, b, c) = arrayOf(
                (Color.red(x) - Color.red(this)).toDouble(),
                (Color.green(x) - Color.green(this)).toDouble(),
                (Color.blue(x) - Color.blue(this)).toDouble()
        )

        return sqrt(a.pow(2.0) + b.pow(2.0) + c.pow(2.0))
    }
}