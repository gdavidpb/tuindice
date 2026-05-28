package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord

data class ObservedRecord(
	val record: AcademicRecord,
	val viewMode: RecordViewMode,
	val selectedTermId: String?,
	val hasSyncedRecord: Boolean
)
