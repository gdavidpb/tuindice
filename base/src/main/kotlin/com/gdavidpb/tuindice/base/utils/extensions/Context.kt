package com.gdavidpb.tuindice.base.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import com.gdavidpb.tuindice.base.utils.ResourcesManager

@ColorInt
fun Context.getCompatColor(@ColorRes colorRes: Int): Int {
	return ResourcesManager.getColor(colorRes, this)
}

fun Context.sharedPreferences(): SharedPreferences {
	return PreferenceManager.getDefaultSharedPreferences(this)
}