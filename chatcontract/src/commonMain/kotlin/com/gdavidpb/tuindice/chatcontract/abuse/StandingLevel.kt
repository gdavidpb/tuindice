package com.gdavidpb.tuindice.chatcontract.abuse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How far the user's standing with the assistant has dropped, from none to banned. */
@Serializable
public enum class StandingLevel {
	@SerialName("none")
	NONE,

	@SerialName("warning")
	WARNING,

	@SerialName("cooldown")
	COOLDOWN,

	@SerialName("suspended")
	SUSPENDED,

	@SerialName("banned")
	BANNED
}
