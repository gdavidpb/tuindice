package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent

/**
 * The official current term among the receiver's terms: the one of kind [TermKind.CURRENT] with the
 * highest [termOrder], ties broken by the highest [id]. Null when no term is current. It is the
 * definition the record's schedule icon and sheet, the schedule and the evaluations read; callers
 * adapt their own term type through the three selectors. It is not the only selection in the app:
 * `enrollmentproof` keeps its own, the first CURRENT term in the order its DAO returns them.
 */
fun <T> Iterable<T>.currentTermOrNull(
	kind: (T) -> TermKind,
	termOrder: (T) -> Int,
	id: (T) -> String
): T? {
	return filter { term -> kind(term).isCurrent }
		.maxWithOrNull(compareBy(termOrder, id))
}
