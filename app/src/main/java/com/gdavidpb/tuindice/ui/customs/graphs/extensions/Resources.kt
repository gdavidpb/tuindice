package com.gdavidpb.tuindice.ui.customs.graphs.extensions

import android.content.Context
import android.content.res.TypedArray
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.gdavidpb.tuindice.utils.extensions.getFloat
import com.gdavidpb.tuindice.utils.extensions.getInt

fun TypedArray.resolveInt(
        context: Context,
        @StyleableRes index: Int,
        @DimenRes defValue: Int
): Int = getResourceId(index, defValue)
        .let { resId -> context.getInt(resId) }

fun TypedArray.resolveFloat(
        context: Context,
        @StyleableRes index: Int,
        @DimenRes defValue: Int
): Float = getResourceId(index, defValue)
        .let { resId -> context.getFloat(resId) }

fun TypedArray.resolveInterpolator(
        context: Context,
        @StyleableRes index: Int,
        @AnimRes defValue: Int
): Interpolator = getResourceId(index, defValue)
        .let { resId -> AnimationUtils.loadInterpolator(context, resId) }

@ColorInt
fun TypedArray.resolveColor(
        context: Context,
        @StyleableRes index: Int,
        @ColorRes defValue: Int
): Int = getResourceId(index, defValue)
        .let { resId -> ContextCompat.getColor(context, resId) }

fun TypedArray.resolveDimension(
        context: Context,
        @StyleableRes index: Int,
        @DimenRes defValue: Int
): Float = getResourceId(index, defValue)
        .let { resId -> context.resources.getDimension(resId) }