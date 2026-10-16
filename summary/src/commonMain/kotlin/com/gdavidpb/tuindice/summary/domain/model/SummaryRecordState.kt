package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord

/**
 * What the hub reads of the record for the moment it is asked: a ready record, the new student the university has no
 * record for yet, or any other state of the record, which leaves only the career. The new student is not an error: the
 * first term has not started, so the hub says so instead of saying nothing.
 */
sealed interface SummaryRecordState {
	/** The record is ready: the week and the term are read from it. */
	data class Ready(
		val record: AcademicRecord
	) : SummaryRecordState

	/** A new student whose university has no record for them yet: the hub says they have no term yet. */
	data object NewStudentWithoutRecord : SummaryRecordState

	/** The record is unavailable, denied, failed or not read yet: only the career remains. */
	data object NotReady : SummaryRecordState
}
