package com.gdavidpb.tuindice.assistant.domain.model

import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode

/**
 * Why a turn failed: the [code] the run reported, whether a retry may help, and the wait it asked, if any.
 */
data class AssistantTurnFailure(
	val code: RunErrorCode,
	val retryable: Boolean,
	val retryAfterSeconds: Int?
)
