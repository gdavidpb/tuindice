package com.gdavidpb.tuindice.data.utils

import android.content.Context
import android.util.SparseIntArray
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

open class ResourcesManager(
        private val context: Context
) {
    private val colorCache = SparseIntArray()

    fun getColor(@ColorRes colorRes: Int): Int {
        return colorCache.get(colorRes, Int.MAX_VALUE).let {
            if (it == Int.MAX_VALUE) {
                val color = ContextCompat.getColor(context, colorRes)

                colorCache.put(colorRes, color)

                color
            } else
                it
        }
    }
}