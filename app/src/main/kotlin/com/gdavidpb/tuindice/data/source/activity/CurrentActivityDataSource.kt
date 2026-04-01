package com.gdavidpb.tuindice.data.source.activity

import android.app.Activity
import java.lang.ref.WeakReference

class CurrentActivityDataSource {
	private var activityRef: WeakReference<Activity>? = null

	fun get(): Activity? {
		return activityRef?.get()
	}

	fun set(activity: Activity) {
		activityRef = WeakReference(activity)
	}
}
