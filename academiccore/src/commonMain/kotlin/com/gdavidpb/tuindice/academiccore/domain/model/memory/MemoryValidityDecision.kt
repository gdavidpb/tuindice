package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.serialization.Serializable

/** What the caller must do with a memory after the engine has judged it. */
@Serializable
sealed interface MemoryValidityDecision {
	/** The memory keeps its state. */
	@Serializable
	data object Keep : MemoryValidityDecision

	/** The memory may no longer hold: the user confirms it before it is used again. */
	@Serializable
	data class RequestConfirmation(val reason: MemoryValidityReason) : MemoryValidityDecision

	/** The memory no longer holds and is archived. */
	@Serializable
	data class Archive(val reason: MemoryValidityReason) : MemoryValidityDecision
}
