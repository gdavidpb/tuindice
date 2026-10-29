package com.gdavidpb.tuindice.platform.android

import android.Manifest
import androidx.activity.result.ActivityResultLauncher

// The launcher of the notification permission. Each activity registers its own before it starts, attaches it here and
// detaches it when it is destroyed, so no launcher, which holds an activity's registry, outlives that activity.
class NotificationPermissionLauncher {
	private var launcher: ActivityResultLauncher<String>? = null

	fun attach(launcher: ActivityResultLauncher<String>) {
		this.launcher = launcher
	}

	// Unregisters the launcher of the activity that is destroyed. It stops being the attached one only when it is that
	// one: a newer activity may have attached its own before this one was destroyed, and that one keeps the offer.
	fun detach(launcher: ActivityResultLauncher<String>) {
		launcher.unregister()

		if (this.launcher === launcher) {
			this.launcher = null
		}
	}

	// Without an attached launcher the request cannot be shown: the offer is simply not made again.
	fun launch() {
		launcher?.launch(Manifest.permission.POST_NOTIFICATIONS)
	}
}
