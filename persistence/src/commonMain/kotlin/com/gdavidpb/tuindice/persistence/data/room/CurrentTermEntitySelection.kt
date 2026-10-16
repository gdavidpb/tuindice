package com.gdavidpb.tuindice.persistence.data.room

import com.gdavidpb.tuindice.academiccore.domain.engine.currentTermOrNull
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity

/**
 * The official current term among stored terms, chosen with the one definition the whole app
 * shares ([currentTermOrNull]: highest term order, ties broken by the highest id). Modules that
 * read terms straight from the DAO use it so none keeps its own rule.
 */
fun List<AcademicTermEntity>.currentTermEntityOrNull(): AcademicTermEntity? {
	return currentTermOrNull(
		kind = { term -> TermKind.valueOf(term.kind) },
		termOrder = AcademicTermEntity::termOrder,
		id = AcademicTermEntity::id
	)
}
