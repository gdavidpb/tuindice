package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.currentTermOrNull
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic

/**
 * Whether the term with [termId] is the current one, the one [currentTermOrNull] picks: the term the enrollment
 * proof is about. Asked of the record as stored, the way [hasScheduleOnTerm] asks it; the projection keeps every
 * term's kind, order and id, so the answer is the same either way.
 */
internal fun AcademicRecord.isCurrentTerm(termId: String): Boolean {
	val currentTerm = terms.currentTermOrNull(
		kind = AcademicTerm::kind,
		termOrder = AcademicTerm::termOrder,
		id = AcademicTerm::id
	)

	return currentTerm?.id == termId
}

/**
 * Whether the term with [termId] is one the student created, a synthetic term: the one the edit and delete
 * actions are about. Asked of the record as stored; the projection keeps every term's kind and id.
 */
internal fun AcademicRecord.isSyntheticTerm(termId: String): Boolean {
	return terms.any { term -> term.id == termId && term.kind.isSynthetic }
}
