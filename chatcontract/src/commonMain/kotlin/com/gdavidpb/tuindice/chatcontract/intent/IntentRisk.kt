package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How risky it is to confirm an intent. */
@Serializable
public enum class IntentRisk {
	@SerialName("normal")
	NORMAL,

	@SerialName("destructive")
	DESTRUCTIVE,

	@SerialName("credential")
	CREDENTIAL
}
