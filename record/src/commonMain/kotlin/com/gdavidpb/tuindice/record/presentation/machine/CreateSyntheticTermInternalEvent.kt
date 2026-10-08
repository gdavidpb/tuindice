package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem

/**
 * Internal machine inputs for the synthetic-term editor: flattened emissions of the
 * three observation pipelines over the draft registers (creation snapshot, debounced
 * subject search, and debounced load preview) plus the submit lifecycle.
 *
 * The search events, and the search results of the snapshot, carry the query they belong to; the
 * machine ignores the ones whose query is no longer the typed one (compared normalized), because
 * `flatMapLatest` does not cancel events that are already queued.
 */
sealed interface CreateSyntheticTermInternalEvent {
	data class SnapshotObserved(
		val editingTermId: String?,
		val editingTermKey: String?,
		val periodOptions: List<SyntheticTermPeriodOption>,
		val selectedPeriod: SyntheticTermPeriodOption?,
		val selectedSubjects: List<CreateTermSubjectItem>,
		val suggestedSubjects: List<CreateTermSubjectItem>,
		val searchResults: List<CreateTermSubjectItem>,
		val searchQuery: String
	) : CreateSyntheticTermInternalEvent

	data class SearchCleared(
		val query: String
	) : CreateSyntheticTermInternalEvent

	data class SearchStarted(
		val query: String
	) : CreateSyntheticTermInternalEvent

	data class SearchSucceeded(
		val query: String
	) : CreateSyntheticTermInternalEvent

	data class SearchFailed(
		val query: String
	) : CreateSyntheticTermInternalEvent

	data object LoadPreviewCleared : CreateSyntheticTermInternalEvent

	data object LoadPreviewStarted : CreateSyntheticTermInternalEvent

	data class LoadPreviewLoaded(
		val preview: SyntheticTermLoadPreview
	) : CreateSyntheticTermInternalEvent

	data object LoadPreviewFailed : CreateSyntheticTermInternalEvent

	data object SubmitStarted : CreateSyntheticTermInternalEvent

	data object SubmitSucceeded : CreateSyntheticTermInternalEvent

	data class SubmitFailed(
		val error: UiText
	) : CreateSyntheticTermInternalEvent
}
