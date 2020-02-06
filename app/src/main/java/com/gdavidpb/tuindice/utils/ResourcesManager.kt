package com.gdavidpb.tuindice.utils

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.ColorRes
import com.gdavidpb.tuindice.utils.extensions.getCompatColor

object ResourcesManager {

    private val cachedColors = hashMapOf<Int, Int>()
    private val cachedFonts = hashMapOf<String, Typeface>()

    fun getColor(@ColorRes colorRes: Int, context: Context) =
            cachedColors.getOrPut(colorRes) {
                context.getCompatColor(colorRes)
            }

    fun getFont(assetPath: String, context: Context) =
            cachedFonts.getOrPut(assetPath) {
                Typeface.createFromAsset(context.assets, assetPath)
            }
}
