package com.gdavidpb.tuindice.record.data.repository.quarter.source

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.utils.IndexComputationEngine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDataSourceTest {
	private lateinit var room: TuIndiceDatabase
	private lateinit var indexComputationEngine: IndexComputationEngine
	private lateinit var dataSource: RoomDataSource

	@Before
	fun setUp() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		room = Room.inMemoryDatabaseBuilder(context, TuIndiceDatabase::class.java)
			.allowMainThreadQueries()
			.build()

		indexComputationEngine = IndexComputationEngine()
		dataSource = RoomDataSource(
			room = room,
			indexComputationEngine = indexComputationEngine
		)
	}

	@After
	fun tearDown() {
		room.close()
	}

	@Test
	fun getQuarter_whenCacheIsEmpty_returnsNull() = runBlocking {
		val quarter = dataSource.getQuarter("q1")

		assertNull(quarter)
	}

	@Test
	fun saveQuarters_thenGetQuartersFlow_mapsAndCachesQuarters() = runBlocking {
		val localQuarter = createQuarter(
			id = "q1",
			startDate = 1000L,
			subjects = listOf(
				createSubject(id = "s1", quarterId = "q1", code = "MA1111", credits = 4, grade = 5),
				createSubject(id = "s2", quarterId = "q1", code = "FS1111", credits = 3, grade = 4)
			)
		)

		dataSource.saveQuarters(listOf(localQuarter))
		val quarters = dataSource.getQuartersFlow().first { it.isNotEmpty() }
		val cached = dataSource.getQuarter("q1")

		assertEquals(1, quarters.size)

		val quarterFromFlow = quarters.first()
		assertEquals(
			localQuarter.copy(subjects = emptyList()),
			quarterFromFlow.copy(subjects = emptyList())
		)
		assertEquals(
			localQuarter.subjects.map { it.id }.toSet(),
			quarterFromFlow.subjects.map { it.id }.toSet()
		)

		assertEquals(
			localQuarter.copy(subjects = emptyList()),
			cached?.copy(subjects = emptyList())
		)
		assertEquals(
			localQuarter.subjects.map { it.id }.toSet(),
			cached?.subjects?.map { it.id }?.toSet()
		)
	}

	@Test
	fun removeQuarter_deletesQuarterAndUpdatesCacheAfterFlowEmission() = runBlocking {
		val localQuarter = createQuarter(
			id = "q1",
			startDate = 1000L,
			subjects = listOf(
				createSubject(id = "s1", quarterId = "q1", code = "MA1111", credits = 4, grade = 5)
			)
		)
		dataSource.saveQuarters(listOf(localQuarter))
		dataSource.getQuartersFlow().first { it.size == 1 }

		dataSource.removeQuarter("q1")
		val quarters = dataSource.getQuartersFlow().first { it.isEmpty() }
		val cached = dataSource.getQuarter("q1")

		assertTrue(quarters.isEmpty())
		assertNull(cached)
	}

	@Test
	fun saveSubjects_updatesExistingSubjectGrade() = runBlocking {
		val localQuarter = createQuarter(
			id = "q1",
			startDate = 1000L,
			subjects = listOf(
				createSubject(id = "s1", quarterId = "q1", code = "MA1111", credits = 4, grade = 2)
			)
		)
		dataSource.saveQuarters(listOf(localQuarter))

		dataSource.saveSubjects(
			subjects = listOf(
				createSubject(id = "s1", quarterId = "q1", code = "MA1111", credits = 4, grade = 5)
			)
		)

		val subject = room.subjects.getSubject("s1")

		assertEquals(5, subject.grade)
	}

	@Test
	fun setSubjectGradeAndRecompute_whenQuarterIsMissing_returnsEmptyResult() = runBlocking {
		val result = dataSource.setSubjectGradeAndRecompute(
			qid = "missing-quarter",
			sid = "s1",
			grade = 5
		)

		assertTrue(result.updatedQuarters.isEmpty())
		assertNull(result.updatedTargetQuarter)
	}

	@Test
	fun setSubjectGradeAndRecompute_recomputesTargetAndFollowingQuarters() = runBlocking {
		val quarter1 = createQuarter(
			id = "q1",
			startDate = 1000L,
			subjects = listOf(
				createSubject(id = "s1", quarterId = "q1", code = "MA1111", credits = 4, grade = 2),
				createSubject(id = "s2", quarterId = "q1", code = "FS1111", credits = 2, grade = 3)
			)
		)
		val quarter2 = createQuarter(
			id = "q2",
			startDate = 2000L,
			subjects = listOf(
				createSubject(id = "s3", quarterId = "q2", code = "ID1111", credits = 3, grade = 4)
			)
		)

		dataSource.saveQuarters(listOf(quarter1, quarter2))
		dataSource.getQuartersFlow().first { it.size == 2 }

		val result = dataSource.setSubjectGradeAndRecompute(
			qid = "q1",
			sid = "s1",
			grade = 5
		)
		val updated = result.updatedQuarters

		assertEquals(setOf("q1", "q2"), updated.map { it.id }.toSet())
		assertEquals("q1", result.updatedTargetQuarter?.id)

		val updatedQ1 = updated.first { it.id == "q1" }
		val updatedQ2 = updated.first { it.id == "q2" }

		assertEquals(5, updatedQ1.subjects.first { it.id == "s1" }.grade)
		assertEquals(4.3333, updatedQ1.grade, 0.0001)
		assertEquals(4.3333, updatedQ1.gradeSum, 0.0001)
		assertEquals(6, updatedQ1.credits)
		assertEquals(6, updatedQ1.creditsSum)

		assertEquals(4.0, updatedQ2.grade, 0.0001)
		assertEquals(4.2222, updatedQ2.gradeSum, 0.0001)
		assertEquals(3, updatedQ2.credits)
		assertEquals(9, updatedQ2.creditsSum)

		val updatedSubject = room.subjects.getSubject("s1")
		assertEquals(5, updatedSubject.grade)
		assertEquals(3, room.subjects.getSubject("s2").grade)
		assertEquals(4, room.subjects.getSubject("s3").grade)
	}

	@Test
	fun setSubjectGradeAndRecompute_whenSubjectIsMissing_returnsEmptyResultWithoutWrites() =
		runBlocking {
			val quarter = createQuarter(
				id = "q1",
				startDate = 1000L,
				subjects = listOf(
					createSubject(
						id = "s1",
						quarterId = "q1",
						code = "MA1111",
						credits = 4,
						grade = 2
					)
				)
			)
			dataSource.saveQuarters(listOf(quarter))
			dataSource.getQuartersFlow().first { it.size == 1 }

			val result = dataSource.setSubjectGradeAndRecompute(
				qid = "q1",
				sid = "missing-subject",
				grade = 5
			)

			assertTrue(result.updatedQuarters.isEmpty())
			assertNull(result.updatedTargetQuarter)
			assertEquals(2, room.subjects.getSubject("s1").grade)
		}

	@Test
	fun setSubjectGradeAndRecompute_whenUpdatesAreConcurrent_appliesSerializedWrites() =
		runBlocking {
			val quarter = createQuarter(
				id = "q1",
				startDate = 1000L,
				subjects = listOf(
					createSubject(
						id = "s1",
						quarterId = "q1",
						code = "MA1111",
						credits = 4,
						grade = 1
					)
				)
			)
			dataSource.saveQuarters(listOf(quarter))
			dataSource.getQuartersFlow().first { it.size == 1 }

			val grades = (1..60).map { value -> value % 6 }

			coroutineScope {
				grades
					.mapIndexed { index, grade ->
						async {
							delay(index.toLong())
							dataSource.setSubjectGradeAndRecompute(
								qid = "q1",
								sid = "s1",
								grade = grade
							)
						}
					}
					.awaitAll()
			}

			val finalGrade = room.subjects.getSubject("s1").grade

			assertEquals(grades.last(), finalGrade)
			assertTrue(finalGrade in 0..5)
		}

	@Test
	fun getQuartersFlow_isOrderedByStartDateDescending() = runBlocking {
		val olderQuarter = createQuarter(
			id = "q-old",
			startDate = 1000L,
			subjects = listOf(createSubject(id = "s-old", quarterId = "q-old"))
		)
		val newerQuarter = createQuarter(
			id = "q-new",
			startDate = 2000L,
			subjects = listOf(createSubject(id = "s-new", quarterId = "q-new"))
		)

		dataSource.saveQuarters(listOf(olderQuarter, newerQuarter))
		val fromRoom = room.quarters.getQuartersWithSubjectsFlow()
			.first { it.size == 2 }
			.map { it.toLocalQuarter() }

		assertEquals(listOf("q-new", "q-old"), fromRoom.map { it.id })
	}

	private fun createQuarter(
		id: String,
		startDate: Long,
		subjects: List<LocalSubject>
	) = LocalQuarter(
		id = id,
		name = id,
		startDate = startDate,
		endDate = startDate + 100L,
		grade = 0.0,
		gradeSum = 0.0,
		credits = 0,
		creditsSum = 0,
		isCurrent = false,
		isReadOnly = false,
		subjects = subjects
	)

	private fun createSubject(
		id: String,
		quarterId: String,
		code: String = "MA1111",
		credits: Int = 4,
		grade: Int = 1
	) = LocalSubject(
		id = id,
		quarterId = quarterId,
		code = code,
		name = id,
		credits = credits,
		grade = grade
	)
}
