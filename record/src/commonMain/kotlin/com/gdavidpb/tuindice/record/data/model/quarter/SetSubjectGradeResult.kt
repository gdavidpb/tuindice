package com.gdavidpb.tuindice.record.data.model.quarter

sealed interface SetSubjectGradeResult {
	data class Applied(
		val updatedQuarters: List<LocalQuarter>,
		val updatedTargetQuarter: LocalQuarter,
		val expectedRevision: Long
	) : SetSubjectGradeResult

	data object TargetNotFound : SetSubjectGradeResult
}
