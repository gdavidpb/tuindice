package com.gdavidpb.tuindice.utils.extensions

import android.animation.TimeInterpolator
import android.animation.ValueAnimator

fun ValueAnimator.duration(value: Long) = apply { duration = value }

fun ValueAnimator.interpolator(value: TimeInterpolator) = apply { interpolator = value }

fun ValueAnimator.repeatMode(value: Int) = apply { repeatMode = value }

fun ValueAnimator.repeatCount(value: Int) = apply { repeatCount = value }

@Suppress("UNCHECKED_CAST")
fun <T> ValueAnimator.doOnUpdate(update: (T) -> Unit) = apply {
    addUpdateListener { update(it.animatedValue as T) }
}
