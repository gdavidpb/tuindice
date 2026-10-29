package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnFailure

/**
 * A turn as its events have built it: the last [lastSeq] applied, the label it shows while it runs,
 * its [status], and the [failure] it ended with, when it failed.
 */
data class AssistantTurnState(
	val turnId: String,
	val lastSeq: Long,
	val progressLabel: String? = null,
	val status: AssistantTurnStatus = AssistantTurnStatus.RUNNING,
	val failure: AssistantTurnFailure? = null
)
