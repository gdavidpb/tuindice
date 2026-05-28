package com.gdavidpb.tuindice.record.domain.model

data class SyntheticTermCreationSnapshot(
	val editingTermId: String? = null,
	val editingTermKey: String? = null,
	val periodOptions: List<SyntheticTermPeriodOption>,
	val selectedPeriod: SyntheticTermPeriodOption?,
	val selectedSubjects: List<SyntheticTermSubject>,
	val suggestedSubjects: List<SyntheticTermSubject>,
	val searchResults: List<SyntheticTermSubject>
)
