package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent

/**
 * The official current term among the receiver's terms: the one of kind [TermKind.CURRENT] with the
 * highest [termOrder], ties broken by the highest [id]. Null when no term is current. It is the
 * definition the record's schedule icon and sheet, the schedule, the evaluations and the enrollment
 * proof read; callers adapt their own term type through the three selectors, and the modules that
 * read stored terms use `currentTermEntityOrNull` from `persistence`. No other selection exists.
 */
fun <T> Iterable<T>.currentTermOrNull(
	kind: (T) -> TermKind,
	termOrder: (T) -> Int,
	id: (T) -> String
): T? {
	return filter { term -> kind(term).isCurrent }
		.maxWithOrNull(compareBy(termOrder, id))
}
