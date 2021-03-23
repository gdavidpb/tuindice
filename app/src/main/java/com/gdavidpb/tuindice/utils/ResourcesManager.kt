package com.gdavidpb.tuindice.utils

import android.content.Context
import androidx.annotation.ColorRes
import com.gdavidpb.tuindice.utils.extensions.getCompatColor

object ResourcesManager {
    private val cachedColors = hashMapOf<Int, Int>()

    fun getColor(@ColorRes colorRes: Int, context: Context) =
            cachedColors.getOrPut(colorRes) {
                context.getCompatColor(colorRes)
            }
}
