package com.gdavidpb.tuindice.base.utils.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import com.gdavidpb.tuindice.base.BuildConfig
import java.io.File

@ColorInt
fun Context.getCompatColor(@ColorRes colorRes: Int): Int {
	return ContextCompat.getColor(this, colorRes)
}

fun Context.getCompatDrawable(@DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable? {
	return ContextCompat.getDrawable(this, drawableRes)
		?.let { drawable -> DrawableCompat.wrap(drawable).mutate() }
		?.also { drawable ->
			drawable.setTint(getCompatColor(colorRes))
		}
}

fun Context.sharedPreferences(): SharedPreferences {
	return PreferenceManager.getDefaultSharedPreferences(this)
}

fun Context.canOpenFile(file: File): Boolean {
	return runCatching {
		val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
		val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file)
		val intent = Intent(Intent.ACTION_VIEW).apply {
			setDataAndType(uri, mimeType)
		}

		intent.resolveActivity(packageManager) != null
	}.getOrDefault(false)
}

fun Context.browse(url: String) {
	runCatching {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

		startActivity(intent)
	}
}

fun Context.findActivity(): Activity {
	var context = this
	while (context is ContextWrapper) {
		if (context is Activity) return context
		context = context.baseContext
	}
	error("No activity")
}