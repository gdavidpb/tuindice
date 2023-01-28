package com.gdavidpb.tuindice.record.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.gdavidpb.tuindice.base.ui.custom.ViewHook
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil

class SubjectGradeSeekBar(context: Context, attrs: AttributeSet) : AppCompatSeekBar(context, attrs),
	ViewHook {

	companion object {
		private const val backgroundColor = Color.WHITE

		private val onDrawLocker = AtomicBoolean(false)

		private val tickPaint = Paint().apply { isAntiAlias = true }

		private var tickSize = 0f
	}

	override fun onDraw(canvas: Canvas) {
		onDrawHook(canvas) { canvasHook -> super.onDraw(canvasHook) }

		super.onDraw(canvas)

		if (progress < max) {
			val barSize = (measuredWidth - (paddingLeft + paddingRight)).toFloat()
			val radius = ceil(tickSize / 2f)
			val step = (barSize / max)

			val translateX = paddingLeft.toFloat()
			val halfHeight = (measuredHeight / 2f)

			for (i in progress.inc()..max) canvas.drawCircle(
				(i * step) + translateX,
				halfHeight,
				radius,
				tickPaint
			)
		}
	}

	override fun onDrawHook(canvas: Canvas, superOnDraw: (Canvas) -> Unit) {
		if (!onDrawLocker.compareAndSet(false, true)) return

		val bitmapHook = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
		val canvasHook = Canvas(bitmapHook)
		val progressHook = progress
		val maxHook = max

		canvasHook.drawColor(backgroundColor)

		max = 2
		progress = 1

		superOnDraw(canvasHook)

		max = maxHook
		progress = progressHook

		val barColor = getBarColor(bitmapHook)

		tickPaint.color = barColor

		tickSize = getTickSize(bitmapHook, barColor)

		bitmapHook.recycle()
	}

	private fun getBarColor(bitmap: Bitmap): Int {
		val x = (measuredWidth * 0.75f).toInt()

		return (0 until measuredHeight).map { y -> bitmap.getPixel(x, y) }.distinct()
			.single { it != backgroundColor }
	}

	private fun getTickSize(bitmap: Bitmap, barColor: Int): Float {
		val y = measuredHeight / 2

		return (0 until measuredWidth).reversed().map { x -> bitmap.getPixel(x, y) }
			.takeWhile { it != barColor }.count { it != backgroundColor }.inc().toFloat()
	}
}