package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.gdavidpb.tuindice.R

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

            for (y in 0..(height - 1)) {
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
            val width = (width - (paddingLeft + paddingRight)).toFloat()
            val radius = (resources.getDimension(R.dimen.size_tick) / 2f) + 0.1f
            val step = (width / max)

            for (i in (progress + 1)..max)
                canvas.drawCircle((i * step) + paddingLeft.toFloat(), (height / 2f), radius, paint)
        }
    }
}