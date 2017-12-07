package com.gdavidpb.tuindice.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import com.gdavidpb.tuindice.R
import android.graphics.Bitmap
import android.graphics.Color

class CustomSeekBar(context: Context, attrs: AttributeSet)
    : AppCompatSeekBar(context, attrs) {

    companion object {
        private var paint = Paint()
        private var color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        /* Trick to extract seek bar background color */
        if (color == Color.WHITE) {
            val bitmapHook = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
            val canvasHook = Canvas(bitmapHook)

            canvasHook.drawColor(Color.WHITE)

            super.onDraw(canvasHook)

            val x = (canvas.width - paddingRight - resources.getDimension(R.dimen.size_tick).toInt() - 1)

            for (y in 0..(canvas.height-1)) {
                val colorHook = bitmapHook.getPixel(x, y)

                val r = Color.red(colorHook)
                val g = Color.green(colorHook)
                val b = Color.blue(colorHook)

                if (colorHook != color && (r == g && g == b)) {
                    color = colorHook
                    break
                }
            }

            paint.color = color
        }

        super.onDraw(canvas)

        if (progress < max) {
            val width = (canvas.width - (paddingLeft + paddingRight)).toFloat()
            val radius = (resources.getDimension(R.dimen.size_tick) / 2f) + 0.1f
            val step = (width / max)

            for (i in (progress + 1)..max)
                canvas.drawCircle((i * step) + paddingLeft.toFloat(), (canvas.height / 2f), radius, paint)
        }
    }
}