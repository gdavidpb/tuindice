package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode

data class SyntheticTermSubject(
	val attemptId: String? = null,
	val subjectCode: String,
	val name: String,
	val credits: Int,
	val gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC,
	val availability: SyntheticTermSubjectAvailability = SyntheticTermSubjectAvailability.AVAILABLE,
	val availabilityDetail: SyntheticTermSubjectAvailabilityDetail? = null
) {
	val creditsText: String = "$credits UC"
	val canAdd: Boolean = availability == SyntheticTermSubjectAvailability.AVAILABLE ||
		availability == SyntheticTermSubjectAvailability.BLOCKED ||
		availability == SyntheticTermSubjectAvailability.NOT_IN_PENSUM
}
