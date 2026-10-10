package com.gdavidpb.tuindice.scenariokit.driver

/** Visible rectangle in the same unit (pixels or points) the driver uses for the screen. */
data class ElementBounds(val left: Double, val top: Double, val right: Double, val bottom: Double) {
	val centerX: Double get() = (left + right) / 2.0
	val centerY: Double get() = (top + bottom) / 2.0
}
