package com.gdavidpb.tuindice.record.data.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord

data class VersionedAcademicRecord(
	val revision: Long,
	val record: AcademicRecord
)
