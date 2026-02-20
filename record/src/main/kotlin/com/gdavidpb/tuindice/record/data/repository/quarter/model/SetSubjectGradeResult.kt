package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class SetSubjectGradeResult(
	val updatedQuarters: List<LocalQuarter>,
	val updatedTargetQuarter: LocalQuarter?
)