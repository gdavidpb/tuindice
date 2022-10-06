package com.gdavidpb.tuindice.login.ui.customs

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max

class BackgroundImageView(context: Context, attrs: AttributeSet)
    : AppCompatImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (drawable != null) {
            val width = drawable.intrinsicWidth

            val height = max(MeasureSpec.getSize(heightMeasureSpec), drawable.intrinsicHeight)

            setMeasuredDimension(width, height)
        } else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}