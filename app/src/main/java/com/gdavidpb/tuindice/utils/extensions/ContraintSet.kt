package com.gdavidpb.tuindice.utils.extensions

import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintSet

fun ConstraintSet.connectBottomBottom(@IdRes startID: Int, @IdRes endId: Int) {
    connect(startID, ConstraintSet.BOTTOM, endId, ConstraintSet.BOTTOM)
}

fun ConstraintSet.connectTopTop(@IdRes startID: Int, @IdRes endId: Int) {
    connect(startID, ConstraintSet.TOP, endId, ConstraintSet.TOP)
}