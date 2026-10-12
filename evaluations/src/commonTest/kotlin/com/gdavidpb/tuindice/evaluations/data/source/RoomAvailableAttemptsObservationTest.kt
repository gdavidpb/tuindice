package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The observation of the available attempts, with the two tables as plain sources: it tells "the
 * terms or the attempts changed" and the attempts are read again from both, so a write that changes
 * them together never shows the new term over the old attempts.
 */
class RoomAvailableAttemptsObservationTest {
	private var terms = listOf(term(id = "t1", order = 20261))
	private var attempts = listOf(attempt(id = "a1", termId = "t1", code = "INF-101"))

	private val termChanges = MutableSharedFlow<List<AcademicTermEntity>>(replay = 1)
	private val attemptChanges = MutableSharedFlow<List<AcademicAttemptEntity>>(replay = 1)

	private fun observe() = RoomDatabaseDataSource.observeAvailableAttempts(
		changes = merge(termChanges, attemptChanges),
		readTerms = { terms },
		readAttempts = { attempts }
	)

	private suspend fun commit(newTerms: List<AcademicTermEntity>, newAttempts: List<AcademicAttemptEntity>) {
		terms = newTerms
		attempts = newAttempts
		// Room invalidates both tables once the transaction commits: both flows speak.
		termChanges.emit(newTerms)
		attemptChanges.emit(newAttempts)
	}

	@Test
	fun aWriteThatMovesTheTermAndItsAttemptsTogether_neverShowsTheNewTermOverTheOldAttempts() = runTest {
		val emitted = mutableListOf<List<String>>()
		termChanges.emit(terms)
		attemptChanges.emit(attempts)

		backgroundScope.launch { observe().collect { list -> emitted += list.map { it.id } } }
		runCurrent()

		commit(
			newTerms = listOf(term(id = "t2", order = 20262)),
			newAttempts = listOf(attempt(id = "a2", termId = "t2", code = "FIS-101"))
		)
		runCurrent()

		// No empty list in between (new term over the attempts of the old one), nothing repeated.
		assertEquals(listOf(listOf("a1"), listOf("a2")), emitted)
	}

	@Test
	fun anAttemptThatDisappearsWithinTheSameTerm_isReflectedWithoutMovingTheTerm() = runTest {
		attempts += attempt(id = "a1b", termId = "t1", code = "MAT-101")
		val emitted = mutableListOf<List<String>>()
		termChanges.emit(terms)
		attemptChanges.emit(attempts)

		backgroundScope.launch { observe().collect { list -> emitted += list.map { it.id } } }
		runCurrent()

		attempts = attempts.filterNot { attempt -> attempt.id == "a1b" }
		attemptChanges.emit(attempts)
		runCurrent()

		assertEquals(listOf(listOf("a1", "a1b"), listOf("a1")), emitted)
	}

	@Test
	fun aWriteThatChangesNothingTheTabReads_doesNotEmitAgain() = runTest {
		val emitted = mutableListOf<List<String>>()
		termChanges.emit(terms)
		attemptChanges.emit(attempts)

		backgroundScope.launch { observe().collect { list -> emitted += list.map { it.id } } }
		runCurrent()

		// An attempt of a past term and a qualitative one: neither is available.
		commit(
			newTerms = terms + term(id = "old", order = 20253, kind = TermKind.HISTORICAL),
			newAttempts = attempts + attempt(id = "past", termId = "old", code = "OLD-101")
		)
		runCurrent()

		assertEquals(listOf(listOf("a1")), emitted)
	}

	@Test
	fun selectAvailableAttempts_keepsOnlyTheNumericAttemptsOfTheCurrentTerm() {
		val result = RoomDatabaseDataSource.selectAvailableAttempts(
			terms = listOf(term(id = "t1", order = 20261), term(id = "t0", order = 20253, kind = TermKind.HISTORICAL)),
			attempts = listOf(
				attempt(id = "b", termId = "t1", code = "MAT-101"),
				attempt(id = "a", termId = "t1", code = "INF-101"),
				attempt(id = "q", termId = "t1", code = "QUA-101", gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL),
				attempt(id = "h", termId = "t0", code = "OLD-101")
			)
		)

		assertEquals(listOf("a", "b"), result.map { attempt -> attempt.id })
	}
}

private fun term(
	id: String,
	order: Int,
	kind: TermKind = TermKind.CURRENT
) = AcademicTermEntity(
	id = id,
	periodYear = order / 10,
	periodCode = "JAN_MAR",
	termKey = "$id-$order",
	termOrder = order,
	periodLabel = id,
	kind = kind.name
)

private fun attempt(
	id: String,
	termId: String,
	code: String,
	gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC
) = AcademicAttemptEntity(
	id = id,
	termId = termId,
	subjectCode = code,
	subjectName = code,
	credits = 3,
	positionInTerm = 0,
	gradingMode = gradingMode.name,
	scoreKind = "NUMERIC",
	scoreNumericValue = 15,
	academicOutcome = "IN_PROGRESS",
	academicBadge = "NONE"
)
