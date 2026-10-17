package com.gdavidpb.tuindice.assistant.domain.model

/**
 * Longest texts the user may send, in characters, once trimmed. The appeal's limit is the contract's
 * AbuseAppeal.MAX_LENGTH.
 */
object AssistantInputLimits {
	const val MESSAGE_MAX_LENGTH = 2_000
	const val REPORT_COMMENT_MAX_LENGTH = 500
}
