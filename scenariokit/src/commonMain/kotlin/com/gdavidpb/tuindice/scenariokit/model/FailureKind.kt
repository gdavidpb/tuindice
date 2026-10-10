package com.gdavidpb.tuindice.scenariokit.model

/**
 * Why a scenario stopped. The interpreter assigns the kind in this order:
 *
 * 1. a driver call throws -> [DRIVER_ERROR];
 * 2. `prepareBackend` fails, or a `mockState` step is answered with anything but 2xx -> [BACKEND_UNAVAILABLE];
 * 3. `EnterText` reads back a different text or, for a secure field, a different number of characters, and when the
 *    driver answered false, the field holds characters that are not the start of the text, or more than was asked
 *    (text that nobody sent) -> [TYPED_TEXT_MISMATCH]; or `ExpectRequest` does not find its request and the most
 *    recent one to that route carries the same identifier with another password, or is the only one the route saw
 *    -> [TYPED_TEXT_MISMATCH].
 *    A driver that answered false is not a corrupted text: with no system dialog in front and the app in the
 *    foreground it is [DRIVER_ERROR] when no key was injected and the field changed, when the field holds the start
 *    of the text (a secure field: fewer characters than asked), and when it holds all of it although the driver
 *    answered false after injecting at least one key; an empty or unreadable field, or one that holds all the text
 *    when no key was injected, stays [ASSERTION] ("was refused");
 * 4. any other failed step while the driver reports a system dialog in front ->
 *    [SYSTEM_DIALOG]; or while the app is not in the foreground although the step
 *    needs it (not `Relaunch`, `Foreground`, `WaitBackgrounded`, `ExpectRequest` or a system query) ->
 *    [APP_NOT_RUNNING] (also a failed launch or foreground, and a `WaitBackgrounded` that finds the app dead: its
 *    own step says so, since there the app is out of the foreground by definition);
 * 5. a step that waited for its own target and the target never came (visible, gone,
 *    enabled, checked, backgrounded, scrolled into view, request received) -> [STEP_TIMEOUT];
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
