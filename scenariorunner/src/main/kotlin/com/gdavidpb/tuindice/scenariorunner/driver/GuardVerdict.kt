package com.gdavidpb.tuindice.scenariorunner.driver

/**
 * What the keyboard guard decides about a touch before it looks at the point, from what it could read (YB-3). It is
 * pure: the readings that cost something (the tree, the dump of the input method) come in as functions and are asked
 * only when the decision needs them, so a probe can say which row of the table applies and that nothing else was read.
 *
 * Before waiting ([beforeWaiting]):
 *
 * | windows    | keyboard listed | field with the focus | input method         | verdict            |
 * |------------|-----------------|----------------------|----------------------|--------------------|
 * | unreadable | -               | -                    | shown                | [REFUSE]           |
 * | unreadable | -               | -                    | hidden or unreadable | [PASS_UNREADABLE]  |
 * | readable   | yes             | -                    | -                    | [PASS]             |
 * | readable   | no              | no                   | -                    | [PASS]             |
 * | readable   | no              | yes or unreadable    | hidden               | [PASS_HIDDEN]      |
 * | readable   | no              | yes or unreadable    | shown or unreadable  | [WAIT]             |
 *
 * A focus that cannot be read is asked about as if there were one: a keyboard that may be opening is not skipped on
 * a guess.
 *
 * After waiting for the window to be listed ([afterWaiting]):
 *
 * | listed now | input method before | input method now     | verdict                 |
 * |------------|---------------------|----------------------|-------------------------|
 * | yes        | -                   | -                    | [PASS_AFTER_WAIT]       |
 * | no         | unreadable          | -                    | [PASS_TAKEN_AS_HIDDEN]  |
 * | no         | shown               | hidden               | [PASS_HIDDEN]           |
 * | no         | shown               | shown or unreadable  | [REFUSE]                |
 */
internal enum class GuardVerdict {
	/** The touch goes on; the point is checked against the keyboard window if one is listed. */
	PASS,

	/** The input method says the keyboard is hidden: the touch goes on at once, nothing is waited for. */
	PASS_HIDDEN,

	/** The keyboard was awaited and is listed now: the layout moved while it opened, so the point is taken again. */
	PASS_AFTER_WAIT,

	/** The window never came and the input method could not be asked: taken as hidden, the one guess, and counted. */
	PASS_TAKEN_AS_HIDDEN,

	/** The window list could not be read and the input method did not say "shown": the touch goes on, counted. */
	PASS_UNREADABLE,

	/** A keyboard may be opening: wait for its window to be listed. */
	WAIT,

	/** The touch could press a key: it is not made. */
	REFUSE;

	companion object {
		fun beforeWaiting(
			windowsReadable: Boolean,
			keyboardListed: Boolean,
			fieldFocused: () -> Boolean?,
			inputMethodShown: () -> Boolean?
		): GuardVerdict = when {
			!windowsReadable -> if (inputMethodShown() == true) REFUSE else PASS_UNREADABLE
			keyboardListed || fieldFocused() == false -> PASS
			inputMethodShown() == false -> PASS_HIDDEN
			else -> WAIT
		}

		fun afterWaiting(
			keyboardListed: Boolean,
			shownBefore: Boolean?,
			shownNow: () -> Boolean?
		): GuardVerdict = when {
			keyboardListed -> PASS_AFTER_WAIT
			shownBefore == null -> PASS_TAKEN_AS_HIDDEN
			shownNow() == false -> PASS_HIDDEN
			else -> REFUSE
		}
	}
}
