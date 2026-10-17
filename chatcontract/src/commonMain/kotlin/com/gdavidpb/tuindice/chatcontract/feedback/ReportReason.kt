package com.gdavidpb.tuindice.chatcontract.feedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Why the user reports an assistant message. */
@Serializable
public enum class ReportReason {
	@SerialName("incorrect")
	INCORRECT,

	@SerialName("inappropriate")
	INAPPROPRIATE,

	@SerialName("harmful")
	HARMFUL,

	@SerialName("other")
	OTHER
}
