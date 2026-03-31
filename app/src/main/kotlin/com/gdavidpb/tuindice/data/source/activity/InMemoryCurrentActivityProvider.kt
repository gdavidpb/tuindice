package com.gdavidpb.tuindice.data.source.activity

import android.app.Activity
import com.gdavidpb.tuindice.data.contract.activity.CurrentActivityProvider
import java.lang.ref.WeakReference

class InMemoryCurrentActivityProvider : CurrentActivityProvider {
	private var activityRef: WeakReference<Activity>? = null

	override fun get(): Activity? {
		return activityRef?.get()
	}

	override fun set(activity: Activity) {
		activityRef = WeakReference(activity)
	}
}
