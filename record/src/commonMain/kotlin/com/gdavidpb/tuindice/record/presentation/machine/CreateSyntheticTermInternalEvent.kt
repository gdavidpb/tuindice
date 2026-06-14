package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem

/**
 * Internal machine inputs for the synthetic-term editor: flattened emissions of the
 * three observation pipelines over the draft registers (creation snapshot, debounced
 * subject search, and debounced load preview) plus the submit lifecycle.
 */
sealed interface CreateSyntheticTermInternalEvent {
	data class SnapshotObserved(
		val editingTermId: String?,
		val editingTermKey: String?,
		val periodOptions: List<SyntheticTermPeriodOption>,
		val selectedPeriod: SyntheticTermPeriodOption?,
		val selectedSubjects: List<CreateTermSubjectItem>,
		val suggestedSubjects: List<CreateTermSubjectItem>,
		val searchResults: List<CreateTermSubjectItem>
	) : CreateSyntheticTermInternalEvent

	data object SearchCleared : CreateSyntheticTermInternalEvent

	data object SearchStarted : CreateSyntheticTermInternalEvent

	data object SearchSucceeded : CreateSyntheticTermInternalEvent

	data object SearchFailed : CreateSyntheticTermInternalEvent

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
