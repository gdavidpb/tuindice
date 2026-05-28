package com.gdavidpb.tuindice.pensum.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind

data class AcademicPensumSnapshot(
	val attempts: List<Attempt>
) {
	data class Attempt(
		val id: String,
		val subjectCode: String,
		val subjectName: String,
		val credits: Int,
		val termOrder: Int,
		val positionInTerm: Int,
		val termKind: TermKind,
		val outcome: AttemptOutcome
	)
}
