package com.gdavidpb.tuindice.subjects.presentation.machine

import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem

/**
 * Internal machine inputs for the subject search screen: flattened emissions of the
 * query-driven observation pipeline (normalized distinct + flatMapLatest + debounced
 * remote refresh merged with local results) plus the retry lifecycle.
 */
sealed interface SubjectSearchInternalEvent {
	data class ShortQueryCleared(
		val query: String
	) : SubjectSearchInternalEvent

	data class LocalResultsChanged(
		val query: String,
		val results: List<SubjectSearchResultItem>
	) : SubjectSearchInternalEvent

	data class RemoteSearchStarted(
		val query: String
	) : SubjectSearchInternalEvent

	data object RemoteSearchSucceeded : SubjectSearchInternalEvent

	data object RemoteSearchFailed : SubjectSearchInternalEvent

	data object RetryStarted : SubjectSearchInternalEvent

	data object RetryCleared : SubjectSearchInternalEvent
}
