package com.gdavidpb.tuindice.chatcontract.turn

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import kotlinx.serialization.Serializable

/**
 * The assistant as it reads for the signed-in user: the service [mode] ("on", "guided" or "off"), the consent, the
 * standing and the usage left.
 */
@Serializable
public data class AssistantStatus(
	public val mode: String,
	public val consent: ConsentStatus,
	public val standing: AbuseStanding,
	public val usage: UsageSummary
)
