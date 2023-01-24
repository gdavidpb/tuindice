package com.gdavidpb.tuindice.base.ui.customs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ProgressBar
import java.util.concurrent.atomic.AtomicBoolean

class TopProgressBar(context: Context, attrs: AttributeSet) : ProgressBar(context, attrs),
	ViewHook {

	companion object {
		private const val backgroundColor = Color.WHITE

		private val onDrawLocker = AtomicBoolean(false)

		private var noTopPadding = 0f
	}

	override fun onDraw(canvas: Canvas) {
		onDrawHook(canvas) { canvasHook -> super.onDraw(canvasHook) }

		canvas.translate(0f, -noTopPadding)

		super.onDraw(canvas)
	}

	override fun onDrawHook(canvas: Canvas, superOnDraw: (Canvas) -> Unit) {
		if (!onDrawLocker.compareAndSet(false, true)) return

		val bitmapHook = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvasHook = Canvas(bitmapHook)

		canvasHook.drawColor(backgroundColor)

		superOnDraw(canvasHook)

		val x = width / 2

		noTopPadding = (0 until height).firstOrNull { y ->
			bitmapHook.getPixel(x, y) != backgroundColor
		}?.toFloat() ?: 0f

		bitmapHook.recycle()
	}
}