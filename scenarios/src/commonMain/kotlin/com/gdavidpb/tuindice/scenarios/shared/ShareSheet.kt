package com.gdavidpb.tuindice.scenarios.shared

/** The system's share surface of About, which the two scenarios that open it find and close the same way (YE-1). */
object ShareSheet {
	/** An element of Android's chooser; it goes when the app is brought back. */
	const val ANDROID_ELEMENT = "com.android.intentresolver:id/chooser_container"

	/** The label of the dimmed area of the iOS popover, which closes the sheet when tapped. */
	const val IOS_ELEMENT = "PopoverDismissRegion"

	/**
	 * Where the dimmed area of the iOS sheet lies over a row of About that has a link (the Creative Commons row;
	 * fractions of the screen): the tap that closes the sheet must not reach that row.
	 */
	const val DISMISS_X = 0.5
	const val DISMISS_Y = 0.3
}
