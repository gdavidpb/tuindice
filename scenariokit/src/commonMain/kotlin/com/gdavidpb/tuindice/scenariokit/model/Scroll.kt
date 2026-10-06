package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Direction the content moves; the finger moves the opposite way. */
@Serializable
enum class Scroll {
	@SerialName("contentDown")
	ContentDown,

	@SerialName("contentUp")
	ContentUp,

	@SerialName("contentForward")
	ContentForward,

	@SerialName("contentBackward")
	ContentBackward
}
