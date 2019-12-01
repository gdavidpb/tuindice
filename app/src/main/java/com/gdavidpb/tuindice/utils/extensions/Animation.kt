package com.gdavidpb.tuindice.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline

fun ValueAnimator.animate(
        init: ValueAnimator.() -> Unit,
        update: ValueAnimator.() -> Unit,
        finish: ValueAnimator.(Boolean) -> Unit = { }) {
    init()

    addUpdateListener(update)

    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            animation.removeAllListeners()

            finish(false)
        }

        override fun onAnimationCancel(animation: Animator) {
            animation.removeAllListeners()

            finish(true)
        }
    })

    start()
}

fun TextView.animateGrade(value: Double) {
    if (context.isPowerSaveMode()) {
        text = value.formatGrade()
        return
    }

    ValueAnimator.ofFloat(0f, value.toFloat()).animate({
        duration = 1000
        interpolator = DecelerateInterpolator()
    }, {
        text = (animatedValue as Float).toDouble().formatGrade()
    })
}

fun Guideline.animatePercent(value: Float) {
    if (context.isPowerSaveMode()) {
        setGuidelinePercent(value)
        return
    }

    ValueAnimator.ofFloat(0f, value).animate({
        duration = 1000
        interpolator = DecelerateInterpolator()
    }, {
        setGuidelinePercent(animatedValue as Float)
    })
}

fun View.lookAtMe(factor: Float = 3f) {
    ValueAnimator.ofFloat(-factor, factor).animate({
        duration = (factor * 100).toLong()
        interpolator = CycleInterpolator(factor)
    }, {
        translationX = animatedValue as Float
    })
}

fun <T : View> T.updateAtMe(update: T.() -> Unit) {
    if (context.isPowerSaveMode()) {
        update()
        return
    }

    val animator = ValueAnimator.ofFloat(1f, 0f)

    animator.animate({
        duration = 250
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
    }, {
        alpha = animatedValue as Float
    })

    animator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationRepeat(animation: Animator) {
            update()
        }
    })
}

fun View.demoAtMe(): ValueAnimator {
    val animator = ValueAnimator.ofFloat(1f, 1.25f)

    animator.animate({
        duration = 500
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
    }, {
        scaleX = animatedValue as Float
        scaleY = animatedValue as Float
    })

    return animator
}