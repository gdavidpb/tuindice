package com.gdavidpb.tuindice.base.domain.model.assistant

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/** Facts known by the opening screen. Null means that the screen has not established the answer. */
@Serializable
@Immutable
data class AssistantSuggestionFacts(
	val hasCurrentTerm: Boolean? = null,
	val hasVisibleAttention: Boolean? = null,
	val hasCreditPace: Boolean? = null,
	val hasIndexHistory: Boolean? = null,
	val hasBestTerm: Boolean? = null,
	val canProjectRemainingTerms: Boolean? = null,
	val selectedTermIsCurrent: Boolean? = null,
	val selectedTermIsLatestClosed: Boolean? = null,
	val selectedWeekIsCurrent: Boolean? = null,
	val hasEvaluations: Boolean? = null,
	val hasAvailableSubjects: Boolean? = null
)
