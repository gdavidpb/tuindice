package com.gdavidpb.tuindice.utils.extensions

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

fun View.matrixAnimation(): ViewPropertyAnimator = animate().apply { cancel() }

fun View.animateScaleDown() = matrixAnimation()
        .setDuration(300)
        .setInterpolator(DecelerateInterpolator())
        .scaleX(0f)
        .scaleY(0f)
        .withEndAction { isInvisible = true }
        .start()

fun View.animateScaleUp() = matrixAnimation()
        .setDuration(200)
        .setInterpolator(AccelerateInterpolator())
        .scaleX(1f)
        .scaleY(1f)
        .withStartAction { isVisible = true }
        .start()

fun View.animateLookAtMe(factor: Float = 3f) = matrixAnimation()
        .setDuration((factor * 100).toLong())
        .setInterpolator(CycleInterpolator(factor))
        .translationX(-factor)
        .translationX(factor)
        .start()

fun Guideline.animatePercent(value: Float) = ValueAnimator.ofFloat(0f, value)
        .duration(700)
        .interpolator(DecelerateInterpolator())
        .doOnUpdate<Float> { percent -> setGuidelinePercent(percent) }
        .start()

fun View.animateZoomInOut() = ValueAnimator.ofFloat(.85f, 1f)
        .duration(1000)
        .repeatMode(ValueAnimator.REVERSE)
        .repeatCount(ValueAnimator.INFINITE)
        .interpolator(AccelerateInterpolator())
        .doOnUpdate<Float> { scale -> scaleX = scale; scaleY = scale }
        .start()

fun View.animateShake() = ValueAnimator.ofFloat(0f, 5f)
        .duration(500)
        .repeatMode(ValueAnimator.REVERSE)
        .interpolator(CycleInterpolator(3f))
        .doOnUpdate<Float> { shake -> rotation = shake }
        .start()

fun TextView.animateGrade(value: Int) {
    val from = "$text".toIntOrNull() ?: 0

    if (from == value && text.isNotEmpty()) return

    ValueAnimator.ofInt(from, value)
            .duration(750)
            .interpolator(DecelerateInterpolator())
            .doOnUpdate<Int> { grade -> text = grade.formatGrade() }
            .start()
}

fun TextView.animateGrade(value: Double, decimals: Int) {
    val from = "$text".toFloatOrNull() ?: 0f

    if (from == value.toFloat() && text.isNotEmpty()) return

    ValueAnimator.ofFloat(from, value.toFloat())
            .duration(750)
            .interpolator(DecelerateInterpolator())
            .doOnUpdate<Float> { grade -> text = grade.toDouble().formatGrade(decimals) }
            .start()
}