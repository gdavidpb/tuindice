package com.gdavidpb.tuindice.base.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.preference.PreferenceManager
import com.gdavidpb.tuindice.base.utils.ResourcesManager

@ColorInt
fun Context.getCompatColor(@ColorRes colorRes: Int): Int {
	return ResourcesManager.getColor(colorRes, this)
}

fun Context.sharedPreferences(): SharedPreferences {
	return PreferenceManager.getDefaultSharedPreferences(this)
}