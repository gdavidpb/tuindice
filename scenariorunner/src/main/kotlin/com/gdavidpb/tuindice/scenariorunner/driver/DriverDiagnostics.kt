package com.gdavidpb.tuindice.scenariorunner.driver

import android.os.SystemClock
import android.util.Log
import com.gdavidpb.tuindice.scenariokit.driver.Diagnostics
import com.gdavidpb.tuindice.scenariokit.model.Platform

/** Logging, pauses and failure artifacts; every line also goes to logcat for the failure capture. */
internal class DriverDiagnostics(
	private val session: DeviceSession,
	private val dialogs: SystemDialogs
) : Diagnostics {
	override val platform: Platform = Platform.Android

	override fun log(line: String) {
		Log.i(TAG, line)
	}

	override fun pause(ms: Long) {
		SystemClock.sleep(ms)
	}

	override fun captureFailure(scenarioId: String, stepIndex: Int) {
		FailureArtifacts.capture(session.device, scenarioId, stepIndex)
	}

	override fun systemDialogInFront(): String? = dialogs.inFront()

	private companion object {
		const val TAG = "ScenarioDriver"
	}
}
