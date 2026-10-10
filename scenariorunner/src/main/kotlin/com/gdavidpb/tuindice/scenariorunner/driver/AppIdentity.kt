package com.gdavidpb.tuindice.scenariorunner.driver

import android.content.ComponentName
import android.content.Intent

/** The debug app the runner drives. */
internal object AppIdentity {
	const val ID = "com.gdavidpb.tuindice.debug"
	const val ACTIVITY = "com.gdavidpb.tuindice.ui.activity.DebugMainActivity"

	/** The intent that the launcher sends to the app's activity, with no flags and no extras. */
	fun mainIntent(): Intent =
		Intent(Intent.ACTION_MAIN)
			.addCategory(Intent.CATEGORY_LAUNCHER)
			.setComponent(ComponentName(ID, ACTIVITY))
}
