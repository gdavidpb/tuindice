package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.serialization.Serializable

/** The kind of academic fact a memory hangs from. [NONE] means it hangs from nothing. */
@Serializable
enum class MemoryAnchorKind {
	NONE,
	CAREER,
	TERM,
	ATTEMPT,
	EVALUATION,
	DATE
}
