package com.gdavidpb.tuindice.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import com.gdavidpb.tuindice.R

class CustomSeekBar(context: Context, attrs: AttributeSet)
    : AppCompatSeekBar(context, attrs) {

    private val paint = Paint()

    init {
        paint.color = ContextCompat.getColor(context,
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                    R.color.colorTickMarkL
                else
                    R.color.colorTickMarkH)
    }

    override fun onDraw(canvas: Canvas) {
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