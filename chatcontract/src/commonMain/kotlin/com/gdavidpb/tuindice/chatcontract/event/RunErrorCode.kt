package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Why a turn failed. */
@Serializable
public enum class RunErrorCode {
	@SerialName("transient")
	TRANSIENT,

	@SerialName("quota_exhausted")
	QUOTA_EXHAUSTED,

	@SerialName("timeout")
	TIMEOUT,

	@SerialName("refused")
	REFUSED,

	@SerialName("assistant_disabled")
	ASSISTANT_DISABLED,

	@SerialName("cancelled")
	CANCELLED,

	@SerialName("other")
	OTHER
}
