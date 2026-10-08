package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec

/**
 * Lifecycle of the app under test. Implementations never throw and never clear the
 * app's state: wiping it is the harness's job, before the runner starts.
 */
interface AppControl {
	/**
	 * Cold-starts the app: stops it if it is running, starts it with the [LaunchSpec.arguments] as its launch
	 * arguments (Android intent extras; the iOS launch environment, to which the iOS driver adds the API and web
	 * base URLs) and waits up to 30 s for it to be in front. True when it is. The persisted state of the app is
	 * kept, so a `Relaunch` step in the middle of a scenario resumes with the same session. Android stops the app
	 * even when it cannot tell whether it runs, because the arguments are read only on a cold start; iOS wraps
	 * `XCUIApplication.launch` so that a launch XCTest turns into an exception answers false instead of ending the
	 * runner. [LaunchSpec.mockStates] are not the driver's: the interpreter sets them before the first launch, and a
	 * `mockState` step sets them again whenever a scenario needs the backend in another state in the middle.
	 */
	fun launch(spec: LaunchSpec): Boolean

	/**
	 * Brings the already running app back to the foreground, for when a step left it (an external link, a share
	 * sheet). True when the app is in front on return. Neither driver starts a process that died: both answer false and
	 * say so in the driver log. Android also requires the app to stay in front for a second, asking again each time
	 * another app holds the front, within `Timeouts.Action` (10 s), and writes every such request to the driver log;
	 * iOS activates the app and waits up to 30 s for the foreground state.
	 */
	fun foreground(): Boolean

	/** Stops the app. No step and no interpreter code calls it: a scenario never stops the app on purpose. */
	fun terminate()

	/**
	 * Whether the app is the one in front right now. The interpreter asks it after a failed step to tell a screen
	 * that did not show up from an app that left the foreground (`APP_NOT_RUNNING`), and the driver contract uses
	 * it to check [foreground]. It is false for an app whose process is gone, on both platforms.
	 */
	fun isForeground(): Boolean
}
