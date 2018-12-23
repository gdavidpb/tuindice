package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.widget.ImageView
import android.util.AttributeSet
import android.view.View

class CustomImageView(context: Context, attrs: AttributeSet)
    : ImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (drawable != null) {
            val width = drawable.intrinsicWidth

            val height = Math.max(
                    View.MeasureSpec.getSize(heightMeasureSpec),
                    drawable.intrinsicHeight)

            setMeasuredDimension(width, height)
        } else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}