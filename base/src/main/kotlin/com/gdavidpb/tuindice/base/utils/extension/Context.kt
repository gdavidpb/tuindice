package com.gdavidpb.tuindice.base.utils.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.gdavidpb.tuindice.base.BuildConfig
import java.io.File

fun Context.sharedPreferences(): SharedPreferences {
	return PreferenceManager.getDefaultSharedPreferences(this)
}

fun Context.openFile(file: File): Boolean {
	return runCatching {
		val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file)

		val intent = Intent(Intent.ACTION_VIEW, uri).apply {
			flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
		}

		startActivity(intent)
	}.isSuccess
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

fun Context.hasCamera() =
	packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)


fun Context.findActivity(): Activity {
	var context = this
	while (context is ContextWrapper) {
		if (context is Activity) return context
		context = context.baseContext
	}
	error("No activity")
}