package com.gdavidpb.tuindice.scenariokit.model

/**
 * Why a scenario stopped. The interpreter assigns the kind in this order:
 *
 * 1. a driver call throws -> [DRIVER_ERROR];
 * 2. `prepareBackend` fails, or a `mockState` step is answered with anything but 2xx -> [BACKEND_UNAVAILABLE];
 * 3. `EnterText` reads back a different text (also after the driver refused to type: the field then holds part of
 *    it) or, for a secure field, a different number of characters; or `ExpectRequest` does not find its request and
 *    the most recent one to that route carries the same identifier with another password, or is the only one
 *    the route saw -> [TYPED_TEXT_MISMATCH];
 * 4. any other failed step while the driver reports a system dialog in front ->
 *    [SYSTEM_DIALOG]; or while the app is not in the foreground although the step
 *    needs it (not `Relaunch`, `Foreground`, `ExpectRequest` or a system query) ->
 *    [APP_NOT_RUNNING] (also a failed launch or foreground);
 * 5. a step that waited for its own target and the target never came (visible, gone,
 *    enabled, settled, scrolled into view, request received) -> [STEP_TIMEOUT];
 * 6. everything else (disabled tap, a gesture the driver refused) -> [ASSERTION].
 */
enum class FailureKind {
	ASSERTION,
	TYPED_TEXT_MISMATCH,
	STEP_TIMEOUT,
	DRIVER_ERROR,
	APP_NOT_RUNNING,
	SYSTEM_DIALOG,
	BACKEND_UNAVAILABLE
}
