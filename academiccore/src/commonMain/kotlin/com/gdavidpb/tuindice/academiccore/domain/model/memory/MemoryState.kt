package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.serialization.Serializable

/** Where a memory stands. Only [ACTIVE] and [PENDING_CONFIRMATION] memories are judged for validity. */
@Serializable
enum class MemoryState {
	ACTIVE,
	PENDING_CONFIRMATION,
	ARCHIVED,
	REPLACED,
	DELETED
}
