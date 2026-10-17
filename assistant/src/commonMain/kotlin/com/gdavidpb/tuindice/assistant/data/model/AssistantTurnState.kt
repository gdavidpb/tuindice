package com.gdavidpb.tuindice.assistant.data.model

/**
 * A turn as its events have built it: the last [lastSeq] applied, the label it shows while it runs,
 * and its [status].
 */
data class AssistantTurnState(
	val turnId: String,
	val lastSeq: Long,
	val progressLabel: String? = null,
	val status: AssistantTurnStatus = AssistantTurnStatus.RUNNING
)
