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
import java.util.concurrent.atomic.AtomicBoolean

class CustomSeekBar(context: Context, attrs: AttributeSet)
    : AppCompatSeekBar(context, attrs), ViewHook {

    companion object {
        private const val backgroundColor = Color.WHITE

        private val onDrawLocker = AtomicBoolean(false)

        private lateinit var paint: Paint

        private var tickSize = 0f
    }

    init {
        tickSize = resources.getDimension(R.dimen.size_tick)
    }

    override fun onDraw(canvas: Canvas) {
        onDrawHook(canvas) { canvasHook -> super.onDraw(canvasHook) }

        super.onDraw(canvas)

        if (progress < max) {
            val width = (width - (paddingLeft + paddingRight)).toFloat()
            val radius = (tickSize / 2f) + 0.1f
            val step = (width / max)

            val translateX = paddingLeft.toFloat()
            val halfHeight = (height / 2f)

            for (i in progress.inc()..max)
                canvas.drawCircle((i * step) + translateX, halfHeight, radius, paint)
        }
    }

    override fun onDrawHook(canvas: Canvas, superOnDraw: (Canvas) -> Unit) {
        if (!onDrawLocker.compareAndSet(false, true)) return

        val bitmapHook = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvasHook = Canvas(bitmapHook)
        val progressHook = progress

        canvasHook.drawColor(backgroundColor)

        progress = 0

        superOnDraw(canvasHook)

        progress = progressHook

        val x = (width - paddingRight - tickSize.toInt() - 1)

        paint = (0 until height)
                .map { y -> bitmapHook.getPixel(x, y) }
                .distinct()
                .maxByOrNull { target -> target distanceTo backgroundColor }
                .let { selectedColor ->
                    Paint().apply {
                        color = selectedColor ?: backgroundColor
                    }
                }

        bitmapHook.recycle()
    }
}