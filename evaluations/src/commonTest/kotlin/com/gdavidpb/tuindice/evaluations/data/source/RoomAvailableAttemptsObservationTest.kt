package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationDisplayContext
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
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

	// The database as far as the reads are concerned: a read transaction and a write transaction do
	// not interleave, which is what a snapshot gives the reader.
	private val database = Mutex()
	private val transactionRunner = object : PersistenceTransactionRunner {
		override suspend fun <R> immediate(block: suspend () -> R): R = database.withLock { block() }

		override suspend fun <R> read(block: suspend () -> R): R = database.withLock { block() }
	}

	private fun observe(
		readAttempts: suspend () -> List<AcademicAttemptEntity> = { attempts }
	) = RoomDatabaseDataSource.observeDisplayContext(
		changes = merge(termChanges, attemptChanges),
		transactionRunner = transactionRunner,
		readTerms = { terms },
		readAttempts = readAttempts
	)

	private suspend fun commit(newTerms: List<AcademicTermEntity>, newAttempts: List<AcademicAttemptEntity>) {
		transactionRunner.immediate {
			terms = newTerms
			attempts = newAttempts
		}
		// Room invalidates both tables once the transaction commits, and tells the observers a moment
		// later, not inside the commit: both flows speak then.
		yield()
		termChanges.emit(newTerms)
		attemptChanges.emit(newAttempts)
	}

	@Test
	fun aWriteThatMovesTheTermAndItsAttemptsTogether_neverShowsTheNewTermOverTheOldAttempts() = runTest {
		val emitted = mutableListOf<String>()
		termChanges.emit(terms)
		attemptChanges.emit(attempts)

		backgroundScope.launch { observe().collect { context -> emitted += context.pair() } }
		runCurrent()

		commit(
			newTerms = listOf(term(id = "t2", order = 20262)),
			newAttempts = listOf(attempt(id = "a2", termId = "t2", code = "FIS-101"))
		)
		runCurrent()

		// The term always with its own attempts, in both directions: never t2 over a1, never t1 over a2,
		// no empty list in between, nothing repeated.
		assertEquals(listOf("t1[a1]", "t2[a2]"), emitted)
	}

	// The write lands after the terms were read and before the attempts are: read apart, that is the
	// new attempts under the old term, an empty list in the middle of the change (the tab would pass
	// through "no attempts" and drop the week the student chose).
	@Test
	fun aWriteThatLandsBetweenTheTwoReads_neverShowsAnEmptyListInTheMiddle() = runTest {
		val emitted = mutableListOf<String>()
		var landed = false
		// One initial change only, so the first read is not cancelled by a second change.
		termChanges.emit(terms)

		backgroundScope.launch {
			observe(
				readAttempts = {
					if (!landed) {
						landed = true
						launch {
							commit(
								newTerms = listOf(term(id = "t2", order = 20262)),
								newAttempts = listOf(attempt(id = "a2", termId = "t2", code = "FIS-101"))
							)
						}
						// The writer gets its turn right here.
						yield()
					}

					attempts
				}
			).collect { context -> emitted += context.pair() }
		}
		runCurrent()

		assertEquals(listOf("t1[a1]", "t2[a2]"), emitted)
	}

	@Test
	fun anAttemptThatDisappearsWithinTheSameTerm_isReflectedWithoutMovingTheTerm() = runTest {
		attempts += attempt(id = "a1b", termId = "t1", code = "MAT-101")
		val emitted = mutableListOf<List<String>>()
		termChanges.emit(terms)
		attemptChanges.emit(attempts)

		backgroundScope.launch { observe().collect { context -> emitted += context.attempts.map { it.id } } }
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

		backgroundScope.launch { observe().collect { context -> emitted += context.attempts.map { it.id } } }
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

private fun LocalEvaluationDisplayContext.pair() =
	"${currentTerm?.id}${attempts.map { attempt -> attempt.id }}"

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
