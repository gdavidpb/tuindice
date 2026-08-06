package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

/**
 * Internal machine inputs for the record screen. The observation resolution
 * (content/empty/waiting) is split per outcome so every row keeps a fixed target;
 * the Failed × Waiting row reproduces the keep-current-while-waiting rule.
 */
sealed interface RecordInternalEvent {
	data class RecordContentObserved(
		val viewMode: RecordViewMode,
		val record: AcademicRecord,
		val selectedTermId: String
	) : RecordInternalEvent

	data object RecordEmptyObserved : RecordInternalEvent

	data object RecordWaitingObserved : RecordInternalEvent

	data object RecordObservationFailed : RecordInternalEvent

	data object RecordRefreshStarted : RecordInternalEvent

	data class RecordRefreshFailed(
		val message: String,
		val navigateToOutdatedCredentials: Boolean
	) : RecordInternalEvent

	data class RecordViewModeSet(
		val viewMode: RecordViewMode
	) : RecordInternalEvent

	data object RecordUnauthorized : RecordInternalEvent

	data class AttemptSelectionFailed(
		val message: String
	) : RecordInternalEvent

	data class SyntheticTermDeleted(
		val message: String
	) : RecordInternalEvent

	data class SyntheticTermDeleteFailed(
		val message: String,
		val navigateToOutdatedCredentials: Boolean
	) : RecordInternalEvent

	data class SyntheticTermRejected(
		val message: String
	) : RecordInternalEvent
}
