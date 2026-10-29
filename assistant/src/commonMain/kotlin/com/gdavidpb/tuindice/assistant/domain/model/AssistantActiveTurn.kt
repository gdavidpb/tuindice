package com.gdavidpb.tuindice.assistant.domain.model

/**
 * The last turn of the conversation: the seq applied, the label it shows while it runs, its phase and its failure.
 */
data class AssistantActiveTurn(
	val turnId: String,
	val lastSeq: Long,
	val progressLabel: String?,
	val status: AssistantTurnPhase,
	val failure: AssistantTurnFailure? = null
)
