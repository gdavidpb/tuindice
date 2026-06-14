package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectCatalogCacheDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: SubjectCatalogCacheDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.subjectCatalogCache
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun observeSearch_whenEmpty_emitsEmptyList() = runTest {
		assertEquals(
			emptyList(),
			dao.observeSearch(normalizedQuery = "alg", limit = 10).first()
		)
	}

	@Test
	fun observeSearch_matchesByCodeOrNameSubstring_excludesOthers() = runTest {
		val byCode = catalogSubject(subjectCode = "ALG101", name = "Estructuras Discretas")
		val byName = catalogSubject(subjectCode = "MA2115", name = "Algebra Lineal")
		val excluded = catalogSubject(subjectCode = "FS1111", name = "Fisica I")

		dao.upsertEntities(listOf(byCode, byName, excluded))

		assertEquals(
			setOf(byCode, byName),
			dao.observeSearch(normalizedQuery = "alg", limit = 10).first().toSet()
		)
	}

	@Test
	fun observeSearch_ranksExactCodeThenCodePrefixThenNamePrefixThenNameSubstring() = runTest {
		val exactCode = catalogSubject(subjectCode = "ALG", name = "Estructuras Discretas")
		val codePrefix = catalogSubject(subjectCode = "ALG101", name = "Programacion")
		val namePrefix = catalogSubject(subjectCode = "MA2115", name = "Algebra Lineal")
		val nameSubstring = catalogSubject(subjectCode = "MA1116", name = "Calculo y Algebra")

		dao.upsertEntities(listOf(nameSubstring, namePrefix, exactCode, codePrefix))

		assertEquals(
			listOf(exactCode, codePrefix, namePrefix, nameSubstring),
			dao.observeSearch(normalizedQuery = "alg", limit = 10).first()
		)
	}

	@Test
	fun observeSearch_withinSameRank_ordersByNormalizedCode() = runTest {
		val lowerCode = catalogSubject(subjectCode = "MA1112", name = "Algebra Basica")
		val higherCode = catalogSubject(subjectCode = "MA2115", name = "Algebra Lineal")

		dao.upsertEntities(listOf(higherCode, lowerCode))

		assertEquals(
			listOf(lowerCode, higherCode),
			dao.observeSearch(normalizedQuery = "alg", limit = 10).first()
		)
	}

	@Test
	fun observeSearch_respectsLimit() = runTest {
		val exactCode = catalogSubject(subjectCode = "ALG", name = "Estructuras Discretas")
		val codePrefix = catalogSubject(subjectCode = "ALG101", name = "Programacion")
		val namePrefix = catalogSubject(subjectCode = "MA2115", name = "Algebra Lineal")

		dao.upsertEntities(listOf(namePrefix, codePrefix, exactCode))

		assertEquals(
			listOf(exactCode, codePrefix),
			dao.observeSearch(normalizedQuery = "alg", limit = 2).first()
		)
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				catalogSubject(subjectCode = "MA1112", name = "Algebra Basica"),
				catalogSubject(subjectCode = "MA2115", name = "Algebra Lineal")
			)
		)

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertEquals(
			emptyList(),
			dao.observeSearch(normalizedQuery = "alg", limit = 10).first()
		)
	}

	private fun catalogSubject(
		subjectCode: String,
		name: String,
		normalizedCode: String = subjectCode.lowercase(),
		normalizedName: String = name.lowercase(),
		credits: Int = 4
	) = SubjectCatalogCacheEntity(
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		gradingMode = "Numeric",
		normalizedCode = normalizedCode,
		normalizedName = normalizedName,
		updatedAt = 1L
	)
}
