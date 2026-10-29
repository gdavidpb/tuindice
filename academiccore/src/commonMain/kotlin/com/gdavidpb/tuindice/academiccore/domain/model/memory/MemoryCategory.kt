package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.serialization.Serializable

/** What a memory is about. Each category has its own validity rules, told by the engine that judges memories. */
@Serializable
enum class MemoryCategory {
	PREFERENCE,
	INTEREST,
	CAREER_GOAL,
	TERM,
	SUBJECT,
	DATED_RESTRICTION
}
