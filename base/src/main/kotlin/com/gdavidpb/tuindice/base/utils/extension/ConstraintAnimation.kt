package com.gdavidpb.tuindice.base.utils.extension

import android.transition.ChangeBounds
import android.transition.TransitionManager
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

inline fun ConstraintLayout.beginTransition(@LayoutRes targetLayout: Int,
                                            transition: ChangeBounds.() -> Unit) {
    val constraintSet = ConstraintSet()
    val changeBounds = ChangeBounds().apply(transition)

    constraintSet.clone(context, targetLayout)

    TransitionManager.beginDelayedTransition(this, changeBounds)
    constraintSet.applyTo(this)
}