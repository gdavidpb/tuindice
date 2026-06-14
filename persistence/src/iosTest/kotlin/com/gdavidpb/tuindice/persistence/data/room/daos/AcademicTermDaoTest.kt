package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicTermDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: AcademicTermDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.academicTerms
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getTerms_whenEmpty_returnsEmptyList() = runTest {
		assertEquals(emptyList(), dao.getTerms())
	}

	@Test
	fun upsertEntity_thenGetTerms_returnsRoundTrippedEntity() = runTest {
		val term = term(id = "term-1", termOrder = 1)

		dao.upsertEntity(term)

		assertEquals(listOf(term), dao.getTerms())
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = term(id = "term-1", termOrder = 1, periodLabel = "Enero - Marzo 2024")
		val updated = original.copy(periodLabel = "Abril - Julio 2024", periodCode = "AJ")

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(listOf(updated), dao.getTerms())
	}

	@Test
	fun getTerms_returnsRowsOrderedByTermOrderDescThenIdAsc() = runTest {
		val newest = term(id = "term-b", termOrder = 2)
		val olderFirstById = term(id = "term-a", termOrder = 1)
		val olderSecondById = term(id = "term-c", termOrder = 1)

		dao.upsertEntities(listOf(olderSecondById, newest, olderFirstById))

		assertEquals(
			listOf(newest, olderFirstById, olderSecondById),
			dao.getTerms()
		)
	}

	@Test
	fun observeTermsFlow_emitsRowsOrderedByTermOrderDesc() = runTest {
		assertEquals(emptyList(), dao.observeTermsFlow().first())

		val oldest = term(id = "term-1", termOrder = 1)
		val newest = term(id = "term-2", termOrder = 2)

		dao.upsertEntities(listOf(oldest, newest))

		assertEquals(listOf(newest, oldest), dao.observeTermsFlow().first())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				term(id = "term-1", termOrder = 1),
				term(id = "term-2", termOrder = 2)
			)
		)

		dao.deleteAll()

		assertEquals(emptyList(), dao.getTerms())
	}

	private fun term(
		id: String,
		termOrder: Int,
		termKey: String = "key-$id",
		periodYear: Int = 2024,
		periodCode: String = "EM",
		periodLabel: String = "Enero - Marzo 2024",
		kind: String = "Regular"
	) = AcademicTermEntity(
		id = id,
		periodYear = periodYear,
		periodCode = periodCode,
		termKey = termKey,
		termOrder = termOrder,
		periodLabel = periodLabel,
		kind = kind
	)
}
