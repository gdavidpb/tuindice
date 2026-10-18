package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * The facts of one memory that its validity depends on. [lastUsedOn] is the last day the memory was used and
 * [pendingSince] the day it started waiting for confirmation. [confirmedFact] is the fact the user last confirmed the
 * memory against, as `MemoryConfirmationEngine.confirmationFactOf` names it; null when the user never confirmed it.
 */
@Serializable
data class MemoryValidityInput(
	val category: MemoryCategory,
	val anchor: MemoryAnchor,
	val state: MemoryState,
	val lastUsedOn: LocalDate?,
	val pendingSince: LocalDate?,
	val goalOutcome: MemoryGoalOutcome = MemoryGoalOutcome.OPEN,
	val confirmedFact: String? = null
)
