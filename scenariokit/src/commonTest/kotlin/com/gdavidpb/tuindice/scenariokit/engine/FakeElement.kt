package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds

/** One element of the scripted screen. */
data class FakeElement(
	var visible: Boolean = true,
	var enabled: Boolean = true,
	var text: String? = null,
	var bounds: ElementBounds = CENTERED,
	/** Becomes visible this many virtual milliseconds after a wait starts. */
	var appearsAfterMs: Long? = null,
	/** Hidden until the screen has been swiped this many times. */
	var hiddenUntilSwipes: Int = 0,
	/** Every bounds read moves the element left by this much, like something still animating. */
	var drift: Double = 0.0,
	/** Reads as disabled for this many enabled checks, then as enabled. */
	var enabledAfterChecks: Int = 0,
	/** Visible, but its bounds read as null: the driver cannot say where it is. */
	var unreadableBounds: Boolean = false,
	/** What the next reads of [text] answer, one per read; the last one repeats. Overrides [text] while it lasts. */
	var scriptedReads: MutableList<String?>? = null
) {
	var enabledChecks = 0

	companion object {
		val CENTERED = ElementBounds(450.0, 950.0, 550.0, 1050.0)
		val NEAR_BOTTOM = ElementBounds(450.0, 1850.0, 550.0, 1950.0)
	}
}
