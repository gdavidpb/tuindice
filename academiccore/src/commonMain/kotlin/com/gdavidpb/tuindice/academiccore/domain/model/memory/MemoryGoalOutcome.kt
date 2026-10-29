package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.serialization.Serializable

/** Whether a career goal is still pursued, reached, or out of reach. */
@Serializable
enum class MemoryGoalOutcome {
	OPEN,
	ACHIEVED,
	IMPOSSIBLE
}
