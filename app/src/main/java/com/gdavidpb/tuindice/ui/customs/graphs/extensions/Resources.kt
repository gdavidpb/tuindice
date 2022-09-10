package com.gdavidpb.tuindice.ui.customs.graphs.extensions

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.AnimRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat

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

fun TypedArray.getDrawable(
	context: Context,
	@StyleableRes index: Int,
	@DrawableRes defValue: Int
): Drawable? = getResourceId(index, defValue)
	.let { resId -> ContextCompat.getDrawable(context, resId) }

inline fun <reified T> TypedArray.getEnum(
	@StyleableRes index: Int,
	defValue: T
): T = getInt(index, Int.MIN_VALUE)
	.let { value -> T::class.java.enumConstants?.get(value) } ?: defValue