package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus

data class ObservedRecord(
	val record: AcademicRecord,
	val viewMode: RecordViewMode,
	val selectedTermId: String?,
	val hasSyncedRecord: Boolean,
	// What the last syncs said about the account, read with the record so a notice and the content
	// it explains always come from the same observation.
	val syncStatus: SyncStatus,
	val syncReport: SyncReport
)
