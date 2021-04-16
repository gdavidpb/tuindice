package com.gdavidpb.tuindice.utils

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

object ResourcesManager {
    private val cachedColors = hashMapOf<Int, Int>()

    fun getColor(@ColorRes colorRes: Int, context: Context) =
            cachedColors.getOrPut(colorRes) {
                ContextCompat.getColor(context, colorRes)
            }
}
