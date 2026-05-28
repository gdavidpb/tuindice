package com.gdavidpb.tuindice.platform.android

import android.content.Intent
import androidx.activity.ComponentActivity
import com.gdavidpb.tuindice.e2e.E2eSeedBridge

object AndroidHostStartupHooks {
	fun run(activity: ComponentActivity, intent: Intent?) {
		E2eSeedBridge.seedIfRequested(activity = activity, intent = intent)
	}
}
