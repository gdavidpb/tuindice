package com.gdavidpb.tuindice.utils

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

object ResourcesManager {
    private val colorsCache = hashMapOf<Int, Int>()

    fun getColor(@ColorRes colorRes: Int, context: Context) =
            colorsCache.getOrPut(colorRes) {
                ContextCompat.getColor(context, colorRes)
            }
}
