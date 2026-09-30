package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotKind

data class SyntheticTermSubjectAvailabilityDetail(
	val termLabel: String? = null,
	val missingSubjectCodes: List<String> = emptyList(),
	// Which slot family the subject counts toward, or would have if those slots were not filled.
	val slotKind: AcademicPensumSlotKind? = null
)
