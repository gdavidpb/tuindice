package com.gdavidpb.tuindice.chatcontract.abuse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Why the service flagged a turn or a user. */
@Serializable
public enum class AbuseReasonCode {
	@SerialName("jailbreak")
	JAILBREAK,

	@SerialName("prompt_extraction")
	PROMPT_EXTRACTION,

	@SerialName("harassment")
	HARASSMENT,

	@SerialName("third_party_data")
	THIRD_PARTY_DATA,

	@SerialName("off_domain")
	OFF_DOMAIN,

	@SerialName("academic_dishonesty")
	ACADEMIC_DISHONESTY,

	@SerialName("spam")
	SPAM,

	@SerialName("automation")
	AUTOMATION
}
