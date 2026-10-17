package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Lifecycle of an [Intent], from its proposal to its final state. */
@Serializable
public enum class IntentStatus {
	@SerialName("proposed")
	PROPOSED,

	@SerialName("accepted")
	ACCEPTED,

	@SerialName("dismissed")
	DISMISSED,

	@SerialName("expired")
	EXPIRED,

	@SerialName("superseded")
	SUPERSEDED,

	@SerialName("queued")
	QUEUED,

	@SerialName("confirmed")
	CONFIRMED,

	@SerialName("conflict")
	CONFLICT,

	@SerialName("rejected")
	REJECTED,

	@SerialName("failed")
	FAILED,

	@SerialName("delegated")
	DELEGATED
}
