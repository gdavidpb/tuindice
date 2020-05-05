package com.gdavidpb.tuindice.ui.customs.graphs.extensions

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.*
import androidx.core.content.ContextCompat

fun TypedArray.resolveInterpolator(
        context: Context,
        @StyleableRes index: Int,
        @InterpolatorRes defValue: Int
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
        @ColorRes defValue: Int
): Float = getResourceId(index, defValue)
        .let { resId -> context.resources.getDimension(resId) }

fun resolveFloat(context: Context, @DimenRes resId: Int): Float =
        TypedValue().let { outValue ->
            context.resources.getValue(resId, outValue, true)

            outValue.float
        }

fun resolveInt(context: Context, @DimenRes resId: Int): Int =
        TypedValue().let { outValue ->
            context.resources.getValue(resId, outValue, true)

            outValue.data
        }