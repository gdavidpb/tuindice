package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How an intent ended. */
@Serializable
public enum class OutcomeKind {
	@SerialName("confirmed")
	CONFIRMED,

	@SerialName("queued")
	QUEUED,

	@SerialName("conflict")
	CONFLICT,

	@SerialName("rejected")
	REJECTED,

	@SerialName("cancelled")
	CANCELLED,

	@SerialName("expired")
	EXPIRED,

	@SerialName("failed")
	FAILED,

	@SerialName("delegated")
	DELEGATED
}
