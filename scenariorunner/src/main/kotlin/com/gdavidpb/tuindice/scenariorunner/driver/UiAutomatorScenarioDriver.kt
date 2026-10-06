package com.gdavidpb.tuindice.scenariorunner.driver

import com.gdavidpb.tuindice.scenariokit.driver.AppControl
import com.gdavidpb.tuindice.scenariokit.driver.BackendControl
import com.gdavidpb.tuindice.scenariokit.driver.Diagnostics
import com.gdavidpb.tuindice.scenariokit.driver.ElementProbe
import com.gdavidpb.tuindice.scenariokit.driver.Gestures
import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.TextEntry
import com.gdavidpb.tuindice.scenariorunner.RunConfig

/** The Android driver: UI Automator out of process, with the work split by contract interface. */
internal class UiAutomatorScenarioDriver(
	private val session: DeviceSession = DeviceSession(),
	private val dialogs: SystemDialogs = SystemDialogs(session)
) : ScenarioDriver,
	AppControl by AppLauncher(session),
	ElementProbe by ElementProber(session),
	Gestures by GestureInjector(session),
	TextEntry by TextInjector(session),
	BackendControl by HttpBackend(RunConfig.wiremockUrl),
	Diagnostics by DriverDiagnostics(session, dialogs) {

	/** Clears the previous output and log of [scenarioId] and any dialog left from an earlier run. */
	fun beginScenario(scenarioId: String) {
		FailureArtifacts.reset(scenarioId)
		session.shell("logcat -c")
		dialogs.dismissKnown()
	}
}
