package com.gdavidpb.tuindice.platform.android

import android.Manifest
import androidx.activity.result.ActivityResultLauncher

// The launcher of the notification permission. MainActivity registers it before the activity starts and drops it when
// the activity is destroyed, so the launcher, which holds the activity's registry, never outlives the activity.
class NotificationPermissionLauncher {
	private var launcher: ActivityResultLauncher<String>? = null

	fun attach(launcher: ActivityResultLauncher<String>) {
		this.launcher = launcher
	}

	fun detach() {
		launcher?.unregister()
		launcher = null
	}

	// Without an attached launcher the request cannot be shown: the offer is simply not made again.
	fun launch() {
		launcher?.launch(Manifest.permission.POST_NOTIFICATIONS)
	}
}
