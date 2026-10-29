package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * The academic state a memory is checked against, as of [today]: the career the user studies, the terms still open,
 * the attempts and evaluations already resolved, and the term each attempt belongs to.
 */
@Serializable
data class MemoryValiditySnapshot(
	val today: LocalDate,
	val careerCode: String?,
	val activeTermKeys: Set<String>,
	val resolvedAttemptIds: Set<String>,
	val attemptTermKeys: Map<String, String>,
	val resolvedEvaluationIds: Set<String>
)
