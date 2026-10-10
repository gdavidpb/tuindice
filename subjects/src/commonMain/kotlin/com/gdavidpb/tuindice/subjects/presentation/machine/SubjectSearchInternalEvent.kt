package com.gdavidpb.tuindice.subjects.presentation.machine

import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem

/**
 * Internal machine inputs for the subject search screen: flattened emissions of the
 * query-driven observation pipeline (normalized distinct + flatMapLatest + debounced
 * remote refresh merged with local results) plus the retry lifecycle.
 *
 * Every event carries the query it belongs to: the machine ignores the ones whose query is no
 * longer the typed one (compared normalized), because neither `flatMapLatest` nor a new query
 * cancels events already queued or the job of a retry.
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

	data class RemoteSearchSucceeded(
		val query: String
	) : SubjectSearchInternalEvent

	data class RemoteSearchFailed(
		val query: String
	) : SubjectSearchInternalEvent

	data class RetryStarted(
		val query: String
	) : SubjectSearchInternalEvent

	data class RetryCleared(
		val query: String
	) : SubjectSearchInternalEvent
}
