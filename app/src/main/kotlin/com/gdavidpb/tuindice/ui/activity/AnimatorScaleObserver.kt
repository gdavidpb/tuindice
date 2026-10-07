package com.gdavidpb.tuindice.ui.activity

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings

/**
 * Calls [onChange] whenever the system animator duration scale changes, between [start] and [stop].
 * The scale lives in developer options and can change while the app is visible (split screen
 * next to Settings, the quick settings tile, `adb shell settings put global`).
 */
internal class AnimatorScaleObserver(
	private val contentResolver: ContentResolver,
	private val onChange: () -> Unit
) {
	private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
		override fun onChange(selfChange: Boolean) {
			onChange()
		}
	}

	fun start() {
		contentResolver.registerContentObserver(
			Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
			false,
			observer
		)
	}

	fun stop() {
		contentResolver.unregisterContentObserver(observer)
	}
}
