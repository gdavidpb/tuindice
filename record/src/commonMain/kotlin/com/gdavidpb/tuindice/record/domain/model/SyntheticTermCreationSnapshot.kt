package com.gdavidpb.tuindice.record.domain.model

data class SyntheticTermCreationSnapshot(
	val periodOptions: List<SyntheticTermPeriodOption>,
	val selectedPeriod: SyntheticTermPeriodOption?,
	val selectedSubjects: List<SyntheticTermSubject>,
	val suggestedSubjects: List<SyntheticTermSubject>,
	val searchResults: List<SyntheticTermSubject>
)
