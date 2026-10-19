package com.gdavidpb.tuindice.academiccore.domain.model

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

// A withdrawn attempt stays out: as in the record's projection, it weighs on nothing, so the pensum reads its subject
// neither approved nor in progress, whatever result it carries.
fun AcademicRecord?.toAcademicPensumSnapshot(): AcademicPensumSnapshot {
	return AcademicPensumSnapshot(
		attempts = this?.terms.orEmpty().flatMap { term ->
			term.attempts.mapIndexedNotNull { positionInTerm, attempt ->
				if (attempt.withdrawn) return@mapIndexedNotNull null
				AcademicPensumSnapshot.Attempt(
					id = attempt.id,
					subjectCode = attempt.subjectCode,
					subjectName = attempt.subjectName,
					credits = attempt.credits,
					termOrder = term.termOrder,
					positionInTerm = positionInTerm,
					termKind = term.kind,
					outcome = attempt.academicOutcome
				)
			}
		}
	)
}
