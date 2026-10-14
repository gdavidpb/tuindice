package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import kotlin.test.Test
import kotlin.test.assertEquals

// What an observation of the content selects: the store's term, unless this visit chose another one and the store
// has not changed since. The visit's choice is what a jump or a tap left on the screen.
class RecordSelectedTermObservationTest {
	private val bothTerms = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(id = TERM_A, periodYear = 2024, attempts = listOf(academicAttempt(subjectCode = "MAT101"))),
			academicTerm(id = TERM_B, periodYear = 2023, attempts = listOf(academicAttempt(subjectCode = "FIS101")))
		)
	)

	@Test
	fun when_theStoreBringsTheSameTermAsBefore_then_theTermOfTheVisitIsKept() {
		val content = content(selected = TERM_B, stored = TERM_A)

		assertEquals(TERM_B, content.selectedTermIdFor(observation(record = bothTerms, selected = TERM_A)))
	}

	@Test
	fun when_theStoreChangedItsTerm_then_theStoreTermWins() {
		val content = content(selected = TERM_A, stored = TERM_A)

		assertEquals(TERM_B, content.selectedTermIdFor(observation(record = bothTerms, selected = TERM_B)))
	}

	@Test
	fun when_theViewModeChanged_then_theStoreTermWins() {
		val content = content(selected = TERM_B, stored = TERM_A, viewMode = RecordViewMode.Historical)

		assertEquals(
			TERM_A,
			content.selectedTermIdFor(
				observation(record = bothTerms, selected = TERM_A, viewMode = RecordViewMode.Projection)
			)
		)
	}

	@Test
	fun when_theTermOfTheVisitIsNoLongerInTheRecord_then_theStoreTermWins() {
		val onlyTermA = AcademicRecord(
			id = "record",
			terms = listOf(academicTerm(id = TERM_A, attempts = listOf(academicAttempt(subjectCode = "MAT101"))))
		)
		val content = content(selected = TERM_B, stored = TERM_A)

		assertEquals(TERM_A, content.selectedTermIdFor(observation(record = onlyTermA, selected = TERM_A)))
	}

	@Test
	fun when_thereIsNoContentYet_then_theStoreTermIsTaken() {
		val loading: Record.State = Record.State.Loading

		assertEquals(TERM_A, loading.selectedTermIdFor(observation(record = bothTerms, selected = TERM_A)))
	}

	private fun content(
		selected: String,
		stored: String,
		viewMode: RecordViewMode = RecordViewMode.Projection
	) = Record.State.Content(
		viewMode = viewMode,
		record = bothTerms,
		selectedTermId = selected,
		storedSelectedTermId = stored
	)

	private fun observation(
		record: AcademicRecord,
		selected: String,
		viewMode: RecordViewMode = RecordViewMode.Projection
	) = RecordInternalEvent.RecordContentObserved(
		viewMode = viewMode,
		record = record,
		selectedTermId = selected,
		notice = null
	)

	private companion object {
		const val TERM_A = "term-a"
		const val TERM_B = "term-b"
	}
}
