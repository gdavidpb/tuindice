package com.gdavidpb.tuindice.assistant.domain.model

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal

/**
 * Longest texts the user may send, in characters, once trimmed. The appeal's limit is the contract's
 * AbuseAppeal.MAX_LENGTH, re-exported here so the UI reads the limits from the domain, not from the wire contract.
 */
object AssistantInputLimits {
	const val MESSAGE_MAX_LENGTH = 2_000
	const val REPORT_COMMENT_MAX_LENGTH = 500
	const val APPEAL_MAX_LENGTH = AbuseAppeal.MAX_LENGTH
}
