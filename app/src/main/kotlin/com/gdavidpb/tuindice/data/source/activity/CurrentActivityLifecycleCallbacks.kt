package com.gdavidpb.tuindice.data.source.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle

class CurrentActivityLifecycleCallbacks(
	private val currentActivityProvider: CurrentActivityProvider
) : Application.ActivityLifecycleCallbacks {
	override fun onActivityResumed(activity: Activity) {
		currentActivityProvider.set(activity)
	}

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
	override fun onActivityStarted(activity: Activity) = Unit
	override fun onActivityPaused(activity: Activity) = Unit
	override fun onActivityStopped(activity: Activity) = Unit
	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
	override fun onActivityDestroyed(activity: Activity) = Unit
}
