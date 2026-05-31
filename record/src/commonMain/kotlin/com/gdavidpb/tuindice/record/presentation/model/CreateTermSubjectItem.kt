package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailabilityDetail

data class CreateTermSubjectItem(
	val subject: SyntheticTermSubject,
	val nameText: String
) {
	val subjectCode: String
		get() = subject.subjectCode

	val creditsText: String
		get() = subject.creditsText

	val availability: SyntheticTermSubjectAvailability
		get() = subject.availability

	val availabilityDetail: SyntheticTermSubjectAvailabilityDetail?
		get() = subject.availabilityDetail

	val canAdd: Boolean
		get() = subject.canAdd
}
