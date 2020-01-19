package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.mappers.distanceTo

class CustomSeekBar(context: Context, attrs: AttributeSet)
    : AppCompatSeekBar(context, attrs) {

    companion object {
        private var defaultColor = Color.WHITE

        private lateinit var paint: Paint
    }

    override fun onDraw(canvas: Canvas) {
        /* Trick to extract seek bar background color */
        if (defaultColor == Color.WHITE) {
            val bitmapHook = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvasHook = Canvas(bitmapHook)
            val progressHook = progress

            canvasHook.drawColor(Color.WHITE)

            progress = 0

            super.onDraw(canvasHook)

            progress = progressHook

            val x = (width - paddingRight - resources.getDimension(R.dimen.size_tick).toInt() - 1)

            defaultColor = (0 until height)
                    .map { y -> bitmapHook.getPixel(x, y) }
                    .distinct()
                    .maxBy { target -> target distanceTo defaultColor } ?: defaultColor

            paint = Paint().apply { color = defaultColor }
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
}