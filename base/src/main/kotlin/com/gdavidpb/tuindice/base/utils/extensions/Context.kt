package com.gdavidpb.tuindice.base.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager

@ColorInt
fun Context.getCompatColor(@ColorRes colorRes: Int): Int {
	return ContextCompat.getColor(this, colorRes)
}

fun Context.getDrawable(@DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable? {
	return ContextCompat.getDrawable(this, drawableRes)
		?.let { drawable -> DrawableCompat.wrap(drawable).mutate() }
		?.also { drawable ->
			drawable.setTint(getCompatColor(colorRes))
		}
}

fun Context.sharedPreferences(): SharedPreferences {
	return PreferenceManager.getDefaultSharedPreferences(this)
}