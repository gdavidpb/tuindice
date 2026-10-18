package com.gdavidpb.tuindice.assistant.data.model

/**
 * How a turn stands in the conversation: still running, finished, or failed. A turn is unconfirmed when its stream ended
 * before its final event and a check from the last seq reached no final event either: its result is unknown.
 */
enum class AssistantTurnStatus {
	RUNNING,
	FINISHED,
	FAILED,
	UNCONFIRMED
}
