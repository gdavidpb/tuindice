package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor

// An evaluation is listed under its subject, and the two are read from different places: the
// subjects from the record's current term, the evaluations from their own store. When they are out
// of step (a subject withdrawn or swapped during the enrollment corrections, a record refreshed
// before the evaluations) an evaluation has no subject to be listed under. It is left out: the
// rest of the list is still worth showing, and failing here would take the whole screen with it.
// The Evaluations tab and the shared visible-evaluations contract both filter through this.
fun List<Evaluation>.listedUnder(attempts: List<EditableAttemptDescriptor>): List<Evaluation> {
	val attemptIds = attempts.mapTo(HashSet()) { attempt -> attempt.id }

	return filter { evaluation -> evaluation.attemptId in attemptIds }
}
