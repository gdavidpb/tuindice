package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How a step finds an element; each driver resolves it with its own toolkit. */
@Serializable
sealed interface Query {
	/** The app's `testTag`, exposed as resource id on Android and accessibility identifier on iOS. */
	@Serializable
	@SerialName("tag")
	data class Tag(val value: String) : Query

	/** Visible text; exact unless [contains] is set. */
	@Serializable
	@SerialName("text")
	data class Text(val value: String, val contains: Boolean = false) : Query

	/** OS-level element (alert button, permission sheet) outside the app's tree. */
	@Serializable
	@SerialName("system")
	data class System(val value: String) : Query
}
