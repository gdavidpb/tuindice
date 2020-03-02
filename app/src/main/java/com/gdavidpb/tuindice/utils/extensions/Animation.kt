package com.gdavidpb.tuindice.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import com.gdavidpb.tuindice.utils.TIME_BACKGROUND_ANIMATION

private fun ValueAnimator.animate(
        view: View,
        init: ValueAnimator.() -> Unit,
        update: ValueAnimator.() -> Unit,
        finish: () -> Unit = { }) {
    val isAnimationAvailable = !view.context.isPowerSaveMode()

    if (isAnimationAvailable) {
        init()

        addUpdateListener(update)

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                animation.removeAllListeners()

                runCatching { finish() }
            }

            override fun onAnimationCancel(animation: Animator) {
                animation.removeAllListeners()

                runCatching { finish() }
            }
        })

        start()
    } else {
        finish()
    }
}

fun TextView.animateGrade(value: Int) {
    val from = "$text".toIntOrNull() ?: 0

    if (from == value && text.isNotEmpty()) return

    ValueAnimator.ofInt(from, value).animate(this, {
        duration = 750
        interpolator = DecelerateInterpolator()
    }, {
        text = (animatedValue as Int).formatGrade()
    }, {
        text = value.formatGrade()
    })
}

fun TextView.animateGrade(value: Double, decimals: Int) {
    val from = "$text".toFloatOrNull() ?: 0f

    if (from == value.toFloat() && text.isNotEmpty()) return

    ValueAnimator.ofFloat(from, value.toFloat()).animate(this, {
        duration = 750
        interpolator = DecelerateInterpolator()
    }, {
        text = (animatedValue as Float).toDouble().formatGrade(decimals)
    }, {
        text = value.formatGrade(decimals)
    })
}

fun Guideline.animatePercent(value: Float) {
    ValueAnimator.ofFloat(0f, value).animate(this, {
        duration = 750
        interpolator = DecelerateInterpolator()
    }, {
        setGuidelinePercent(animatedValue as Float)
    }, {
        setGuidelinePercent(value)
    })
}

fun View.animateLookAtMe(factor: Float = 3f) {
    ValueAnimator.ofFloat(-factor, factor).animate(this, {
        duration = (factor * 100).toLong()
        interpolator = CycleInterpolator(factor)
    }, {
        translationX = animatedValue as Float
    })
}

fun View.animateZoomInOut() {
    val animator = ValueAnimator.ofFloat(.85f, 1f)

    animator.animate(this, {
        duration = 1000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateInterpolator()
    }, {
        val scale = animatedValue as Float

        scaleX = scale
        scaleY = scale
    })
}

fun View.animateShake() {
    ValueAnimator.ofFloat(0f, 5f).animate(this, {
        duration = 500
        repeatMode = ValueAnimator.REVERSE
        interpolator = CycleInterpolator(3f)
    }, {
        rotation = animatedValue as Float
    })
}

fun Pair<View, View>.animateInfiniteLoop() {
    ValueAnimator.ofFloat(0f, 1f).animate(first, {
        duration = TIME_BACKGROUND_ANIMATION
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
    }, {
        val width = first.width
        val translationX = (width * animatedFraction)

        first.translationX = translationX
        second.translationX = (translationX - width)
    })
}