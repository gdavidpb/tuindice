package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.serialization.Serializable

/** Why the engine asks to confirm a memory or archives it. */
@Serializable
enum class MemoryValidityReason {
	UNUSED,
	CAREER_CHANGED,
	GOAL_DATE_PASSED,
	GOAL_ACHIEVED,
	GOAL_IMPOSSIBLE,
	TERM_CLOSED,
	ATTEMPT_RESOLVED,
	EVALUATION_RESOLVED,
	DATE_PASSED,
	UNCONFIRMED
}
