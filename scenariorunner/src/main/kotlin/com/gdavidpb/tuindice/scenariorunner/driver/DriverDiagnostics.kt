package com.gdavidpb.tuindice.scenariorunner.driver

import android.os.SystemClock
import com.gdavidpb.tuindice.scenariokit.driver.Diagnostics
import com.gdavidpb.tuindice.scenariokit.model.Platform

/** Logging, pauses and failure artifacts; every line goes to `driver.log` and to logcat. */
internal class DriverDiagnostics(
	private val session: DeviceSession,
	private val dialogs: SystemDialogs
) : Diagnostics {
	override val platform: Platform = Platform.Android

	override fun log(line: String) {
		session.log.write(line)
	}

	override fun lastRefusal(): String? = session.log.lastRefusal()

	override fun pause(ms: Long) {
		SystemClock.sleep(ms)
	}

	override fun captureFailure(scenarioId: String, stepIndex: Int) {
		FailureArtifacts.capture(session.device, scenarioId, stepIndex)
	}

	override fun systemDialogInFront(): String? = dialogs.inFront()
}
