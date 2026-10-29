package com.gdavidpb.tuindice.scenarios.shared

/**
 * The system's share surface, which every scenario that opens it (About, the conformance suite and the share of
 * Statistics) finds and closes the same way (YE-1).
 */
object ShareSheet {
	/** An element of Android's chooser; it goes when the app is brought back. */
	const val ANDROID_ELEMENT = "com.android.intentresolver:id/chooser_container"

	/** The label of the dimmed area of the iOS popover, which closes the sheet when tapped. */
	const val IOS_ELEMENT = "PopoverDismissRegion"

	/** A point of the dimmed area of the iOS sheet, as fractions of the screen: the share of Statistics closes on it. */
	const val DISMISS_X = 0.5
	const val DISMISS_Y = 0.3

	/**
	 * A point of the dimmed area of the iOS sheet of Settings, as fractions of the screen: the middle of the top bar,
	 * where no control lies (the back button is at its start). The rows under the sheet depend on the scroll position
	 * and on the device, so the tap does not aim at one of them.
	 */
	const val SETTINGS_DISMISS_X = 0.5
	const val SETTINGS_DISMISS_Y = 0.09
}
