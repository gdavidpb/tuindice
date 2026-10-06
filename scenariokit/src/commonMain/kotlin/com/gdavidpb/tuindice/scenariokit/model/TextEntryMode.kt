package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TextEntryMode {
	/** Key events, one per character, like a user typing. */
	@SerialName("keys")
	Keys,

	/** One atomic value assignment. */
	@SerialName("set")
	Set
}
