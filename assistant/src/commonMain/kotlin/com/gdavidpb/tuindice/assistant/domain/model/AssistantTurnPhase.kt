package com.gdavidpb.tuindice.assistant.domain.model

/** How a turn stands: still running, finished, or failed. */
enum class AssistantTurnPhase {
	RUNNING,
	FINISHED,
	FAILED
}
