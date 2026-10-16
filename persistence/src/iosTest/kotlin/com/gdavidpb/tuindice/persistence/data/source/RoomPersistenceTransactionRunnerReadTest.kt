package com.gdavidpb.tuindice.persistence.data.source

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.daos.createInMemoryTuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomPersistenceTransactionRunnerReadTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var runner: RoomPersistenceTransactionRunner

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		runner = RoomPersistenceTransactionRunner(database)
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun read_runsTheDaoReadsOfTheBlockInsideTheTransaction_andReturnsTheirResult() = runTest {
		database.academicTerms.upsertEntity(term(id = "term-1", termOrder = 1))

		val ids = runner.read {
			database.academicTerms.getTerms().map(AcademicTermEntity::id)
		}

		assertEquals(listOf("term-1"), ids)
	}

	@Test
	fun read_seesWhatAnEarlierImmediateTransactionCommitted() = runTest {
		runner.immediate {
			database.academicTerms.upsertEntity(term(id = "term-2", termOrder = 2))
		}

		val terms = runner.read { database.academicTerms.getTerms() }

		assertEquals(listOf("term-2"), terms.map(AcademicTermEntity::id))
	}

	private fun term(id: String, termOrder: Int) = AcademicTermEntity(
		id = id,
		periodYear = 2024,
		periodCode = "EM",
		termKey = "key-$id",
		termOrder = termOrder,
		periodLabel = "Enero - Marzo 2024",
		kind = "Regular"
	)
}
