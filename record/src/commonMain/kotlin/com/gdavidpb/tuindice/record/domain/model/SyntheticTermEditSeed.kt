package com.gdavidpb.tuindice.record.domain.model

data class SyntheticTermEditSeed(
	val termId: String,
	val termKey: String,
	val period: SyntheticTermPeriodOption,
	val subjects: List<SyntheticTermSubject>
)
