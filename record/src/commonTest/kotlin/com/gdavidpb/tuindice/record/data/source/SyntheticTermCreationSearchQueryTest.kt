@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// The machine decides whether the search results of a snapshot are still the ones of the typed query, so
// the snapshot says which query they answer: as it was typed, and also when it is too short to search.
class SyntheticTermCreationSearchQueryTest {
	@Test
	fun observeSnapshot_saysWhichQueryTheSearchResultsAnswer() = runTest {
		val dataSource = dataSource()

		val searched = snapshotOf(dataSource, query = "  MÁTEM ")
		assertEquals("  MÁTEM ", searched.searchQuery)
		assertEquals(listOf("MA1112"), searched.searchResults.map { subject -> subject.subjectCode })

		val tooShort = snapshotOf(dataSource, query = "m")
		assertEquals("m", tooShort.searchQuery)
		assertEquals(emptyList(), tooShort.searchResults)
	}

	private suspend fun snapshotOf(dataSource: SyntheticTermCreationDataSource, query: String) =
		dataSource.observeSnapshot(
			queryFlow = MutableStateFlow(query),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

	private fun dataSource(): SyntheticTermCreationDataSource {
		val cacheKey = "2016-regular"
		val name = "Matemáticas II"

		return SyntheticTermCreationDataSource(
			academicRecordRepository = FakeAcademicRecordRepository(AcademicRecord(id = "record", terms = emptyList())),
			caches = SyntheticTermCreationCaches(
				pensumCacheDao = FakePensumCacheDao(
					cache = PensumCacheEntity(
						cacheKey = cacheKey,
						year = 2016,
						modalityId = "regular",
						payloadJson = pensumPayload(
							nodes = listOf(pensumNode(id = "ma1112", subjectCode = "MA1112", name = name)),
							edges = emptyList()
						),
						updatedAt = 1L
					)
				),
				pensumSelectionDao = FakePensumSelectionDao(
					selection = PensumSelectionEntity(
						year = 2016,
						modalityId = "regular",
						cacheKey = cacheKey,
						updatedAt = 1L
					)
				),
				subjectCatalogCacheDao = FakeSubjectCatalogCacheDao(
					listOf(
						SubjectCatalogCacheEntity(
							subjectCode = "MA1112",
							name = name,
							credits = 4,
							gradingMode = GradingMode.NUMERIC.name,
							normalizedCode = SubjectCatalogSearchNormalizer.normalize("MA1112"),
							normalizedName = SubjectCatalogSearchNormalizer.normalize(name),
							updatedAt = 1L
						)
					)
				)
			),
			ktorClient = HttpClient(MockEngine { respondOk() }),
			json = Json { ignoreUnknownKeys = true },
			clock = Clock.System
		)
	}
}
