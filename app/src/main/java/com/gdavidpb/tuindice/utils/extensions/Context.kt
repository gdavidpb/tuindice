package com.gdavidpb.tuindice.utils.extensions

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
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.ResourcesManager

fun Context.versionName(): String {
    val environmentRes = if (BuildConfig.DEBUG) R.string.debug else R.string.release

    return getString(R.string.app_version,
            getString(environmentRes),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE)
}

@ColorInt
fun Context.getCompatColor(@ColorRes colorRes: Int): Int {
    return ResourcesManager.getColor(colorRes, this)
}

fun Context.getCompatDrawable(
    @DrawableRes resId: Int,
    width: Int = -1,
    height: Int = -1
): Drawable {
    val drawable = AppCompatResources.getDrawable(this, resId) ?: error("resId: $resId")

    return if (width == -1 && height == -1) {
        drawable
    } else {
        DrawableCompat.wrap(drawable).apply {
            bounds = Rect(0, 0, width, height)
        }
    }
}

fun Context.sharedPreferences(): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(this)
}