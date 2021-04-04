package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.AttributeSet
import androidx.cardview.widget.CardView

class CustomCardView(context: Context, attrs: AttributeSet)
    : CardView(context, attrs) {

    companion object {
        private val paint = Paint().apply {
            colorFilter = ColorMatrix()
                    .apply { setSaturation(0f) }
                    .let(::ColorMatrixColorFilter)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else .75f
        requestLayout()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (!isEnabled) canvas.saveLayer(null, paint)

        super.dispatchDraw(canvas)

        if (!isEnabled) canvas.restore()
    }

    override fun draw(canvas: Canvas) {
        if (!isEnabled) canvas.saveLayer(null, paint)

        super.draw(canvas)

        if (!isEnabled) canvas.restore()
    }
}