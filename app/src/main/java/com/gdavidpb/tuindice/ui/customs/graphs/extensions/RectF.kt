package com.gdavidpb.tuindice.ui.customs.graphs.extensions

import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.transform

fun RectF.transform(matrix: Matrix) {
    set(transform(matrix))
}

fun RectF.inset(factor: Float) {
    val computed = 1f - factor

    inset(width() * computed, height() * computed)
}