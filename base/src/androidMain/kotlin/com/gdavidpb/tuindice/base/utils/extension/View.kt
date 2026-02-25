package com.gdavidpb.tuindice.base.utils.extension

import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StyleableRes

fun View.loadAttributes(
	@StyleableRes
	styleId: IntArray,
	attrs: AttributeSet
): TypedArray = context.theme.obtainStyledAttributes(attrs, styleId, 0, 0)