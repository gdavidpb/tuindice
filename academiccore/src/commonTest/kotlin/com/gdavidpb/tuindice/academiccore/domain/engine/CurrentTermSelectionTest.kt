package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrentTermSelectionTest {
	private data class Term(val id: String, val order: Int, val kind: TermKind)

	private fun List<Term>.current() = currentTermOrNull(
		kind = Term::kind,
		termOrder = Term::order,
		id = Term::id
	)?.id

	@Test
	fun picksTheCurrentTermIgnoringTheOthers() {
		val terms = listOf(
			Term("a", 20261, TermKind.CURRENT),
			Term("b", 20262, TermKind.SYNTHETIC),
			Term("c", 20253, TermKind.HISTORICAL)
		)

		assertEquals("a", terms.current())
	}

	@Test
	fun isNullWhenNothingIsCurrent() {
		assertNull(listOf(Term("a", 20261, TermKind.HISTORICAL), Term("b", 20262, TermKind.SYNTHETIC)).current())
		assertNull(emptyList<Term>().current())
	}

	@Test
	fun withSeveralCurrentTermsTheHighestTermOrderWins() {
		val terms = listOf(Term("z", 20261, TermKind.CURRENT), Term("a", 20262, TermKind.CURRENT))

		assertEquals("a", terms.current())
	}

	@Test
	fun aTieOnTermOrderIsBrokenByTheHighestId() {
		val terms = listOf(Term("a", 20261, TermKind.CURRENT), Term("b", 20261, TermKind.CURRENT))

		assertEquals("b", terms.current())
	}
}
