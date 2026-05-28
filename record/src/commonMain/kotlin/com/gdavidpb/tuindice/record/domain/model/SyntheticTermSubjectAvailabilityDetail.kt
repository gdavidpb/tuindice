package com.gdavidpb.tuindice.record.domain.model

data class SyntheticTermSubjectAvailabilityDetail(
	val termLabel: String? = null,
	val missingSubjectCodes: List<String> = emptyList()
)
