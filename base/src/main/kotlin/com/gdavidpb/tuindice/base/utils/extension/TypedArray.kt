package com.gdavidpb.tuindice.base.utils.extension

import android.content.Context
import android.content.res.TypedArray
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.AnimRes
import androidx.annotation.DimenRes
import androidx.annotation.StyleableRes

fun TypedArray.getInterpolator(
	context: Context,
	@StyleableRes index: Int,
	@AnimRes defValue: Int
): Interpolator = getResourceId(index, defValue)
	.let { resId -> AnimationUtils.loadInterpolator(context, resId) }

fun TypedArray.getDimension(
	@StyleableRes index: Int,
	@DimenRes defValue: Int
): Float = getResourceId(index, defValue)
	.let { resId -> resources.getDimension(resId) }