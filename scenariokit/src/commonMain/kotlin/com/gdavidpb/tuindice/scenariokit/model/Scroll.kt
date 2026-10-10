package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Direction the content moves; the finger moves the opposite way. There is no horizontal direction: the only list that
 * could be scrolled sideways, the version options of the pensum dialog, fits the screens the suite runs on (checked on
 * both devices: its last option is in view before the step), so a horizontal scroll would have no effect to check.
 */
@Serializable
enum class Scroll {
	@SerialName("contentDown")
	ContentDown,

	@SerialName("contentUp")
	ContentUp
}
