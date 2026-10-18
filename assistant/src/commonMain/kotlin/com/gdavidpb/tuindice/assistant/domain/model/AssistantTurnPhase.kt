package com.gdavidpb.tuindice.assistant.domain.model

/** How a turn stands: still running, finished, failed, or unconfirmed (its result is unknown). */
enum class AssistantTurnPhase {
	RUNNING,
	FINISHED,
	FAILED,
	UNCONFIRMED
}
