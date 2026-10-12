@file:OptIn(ExperimentalForeignApi::class)

package com.gdavidpb.tuindice.persistence.data.room

import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.source.RoomPersistenceTransactionRunner
import com.gdavidpb.tuindice.persistence.di.createIosDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The isolation of `read` with two real connections: the database is the file the app opens
 * (createIosDatabase: same driver, same journal mode), so the reader and the writer are different
 * connections and a write can commit in the middle of a read. An in-memory database gives one
 * shared connection, which proves nothing about it.
 */
class RoomPersistenceTransactionRunnerIsolationTest {
	private lateinit var path: String
	private lateinit var database: TuIndiceDatabase
	private lateinit var runner: RoomPersistenceTransactionRunner

	@BeforeTest
	fun setUp() {
		path = "${NSTemporaryDirectory().trimEnd('/')}/read-isolation-${NSUUID().UUIDString}.db"
		database = createIosDatabase(path)
		runner = RoomPersistenceTransactionRunner(database)
	}

	@AfterTest
	fun tearDown() {
		database.close()
		listOf(path, "$path-wal", "$path-shm", "$path-journal").forEach { file ->
			NSFileManager.defaultManager.removeItemAtPath(file, error = null)
		}
	}

	// The write that lands between the two reads of a block moves the term and its attempt together.
	// Inside `read` both reads belong to one committed state: the second does not see the write.
	@Test
	fun read_whenAWriteCommitsBetweenItsTwoReads_theSecondReadStillSeesTheStateOfTheFirst() = runTest {
		val seen = runner.read {
			val termsBefore = database.academicTerms.getTerms().map(AcademicTermEntity::id)

			commitFromAnotherConnection()

			val attemptsAfter = database.academicAttempts.getAttempts().map(AcademicAttemptEntity::id)

			termsBefore to attemptsAfter
		}

		assertEquals(emptyList<String>() to emptyList(), seen, "the write that committed meanwhile was not seen")
		// And it did commit: the next read sees it, whole.
		assertEquals(
			listOf("t2") to listOf("a2"),
			runner.read {
				database.academicTerms.getTerms().map(AcademicTermEntity::id) to
					database.academicAttempts.getAttempts().map(AcademicAttemptEntity::id)
			}
		)
	}

	// The control: the same sequence with two separate reads is what the transaction exists to prevent.
	@Test
	fun withoutRead_theSameSequenceSeesTheTermOfBeforeAndTheAttemptOfAfter() = runTest {
		val termsBefore = database.academicTerms.getTerms().map(AcademicTermEntity::id)

		commitFromAnotherConnection()

		val attemptsAfter = database.academicAttempts.getAttempts().map(AcademicAttemptEntity::id)

		assertEquals(emptyList<String>() to listOf("a2"), termsBefore to attemptsAfter)
	}

	// From a scope of its own, so that it does not inherit the connection the read holds.
	private suspend fun commitFromAnotherConnection() {
		CoroutineScope(Dispatchers.Default).async {
			runner.immediate {
				database.academicTerms.upsertEntity(term(id = "t2", termOrder = 20262))
				database.academicAttempts.upsertEntity(attempt(id = "a2", termId = "t2"))
			}
		}.await()
	}

	private fun term(id: String, termOrder: Int) = AcademicTermEntity(
		id = id,
		periodYear = termOrder / 10,
		periodCode = "SEP_DEC",
		termKey = "key-$id",
		termOrder = termOrder,
		periodLabel = id,
		kind = "CURRENT"
	)

	private fun attempt(id: String, termId: String) = AcademicAttemptEntity(
		id = id,
		termId = termId,
		subjectCode = "CI-101",
		subjectName = "CI-101",
		credits = 3,
		positionInTerm = 0,
		gradingMode = "NUMERIC",
		scoreKind = "NUMERIC",
		scoreNumericValue = 15,
		academicOutcome = "IN_PROGRESS",
		academicBadge = "NONE"
	)
}
