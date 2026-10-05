package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice

/**
 * Internal machine inputs for the record screen. The observation resolution
 * (content/empty/waiting) is split per outcome so every row keeps a fixed target;
 * the Failed × Waiting row reproduces the keep-current-while-waiting rule.
 */
sealed interface RecordInternalEvent {
	data class RecordContentObserved(
		val viewMode: RecordViewMode,
		val record: AcademicRecord,
		val selectedTermId: String,
		val notice: RecordNotice?
	) : RecordInternalEvent

	data class RecordEmptyObserved(
		val notice: RecordNotice?
	) : RecordInternalEvent

	// isNewStudentNoRecord: what the same observation says of why the record is still to come.
	data class RecordWaitingObserved(
		val isNewStudentNoRecord: Boolean
	) : RecordInternalEvent

	/** The sync learned (or stopped saying) that the university has no record for this account. */
	data class NewStudentNoRecordObserved(
		val isNewStudentNoRecord: Boolean
	) : RecordInternalEvent

	data object RecordObservationFailed : RecordInternalEvent

	data object RecordRefreshStarted : RecordInternalEvent

	// isNewStudentNoRecord: asked by the refresh as it fails, so the failure arrives with its reason.
	data class RecordRefreshFailed(
		val message: String,
		val navigateToOutdatedCredentials: Boolean,
		val isNewStudentNoRecord: Boolean
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
