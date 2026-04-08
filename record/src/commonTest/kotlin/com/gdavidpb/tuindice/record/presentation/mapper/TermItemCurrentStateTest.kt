package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TermItemCurrentStateTest {
	@Test
	fun isCurrentTerm_returnsTrue_whenBackendMarksTheTermAsCurrent() {
		val term = termProjection(
			current = true,
			editable = true,
			closed = false,
			synthetic = false
		)

		assertTrue(term.isCurrentTerm())
	}

	@Test
	fun isCurrentTerm_returnsFalse_whenBackendDoesNotMarkTheTermAsCurrent() {
		val term = termProjection(
			current = false,
			editable = true,
			closed = false,
			synthetic = false
		)

		assertFalse(term.isCurrentTerm())
	}
}

private fun termProjection(
	current: Boolean,
	editable: Boolean,
	closed: Boolean,
	synthetic: Boolean
) = TermProjection(
	id = "term-id",
	label = "Term",
	startAtMillis = 1_000L,
	endAtMillis = 2_000L,
	order = 0,
	current = current,
	closed = closed,
	editable = editable,
	synthetic = synthetic,
	grade = 0.0,
	gradeSum = 0.0,
	credits = 0,
	creditsSum = 0,
	attempts = emptyList()
)
