package com.gdavidpb.tuindice.pensum.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind

data class AcademicPensumSnapshot(
	val attempts: List<Attempt>
) {
	data class Attempt(
		val subjectCode: String,
		val credits: Int,
		val termKind: TermKind,
		val outcome: AttemptOutcome
	)
}
