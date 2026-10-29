@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// A subject withdrawn from the current term is no longer being taken: the planner offers it as available, with no
// term that would say it is in progress.
class SyntheticTermCreationWithdrawnTest {
	@Test
	fun observeSnapshot_readsSubjectWithdrawnFromCurrentTermAsAvailable() = runTest {
		val snapshot = dataSource().observeSnapshot(
			queryFlow = MutableStateFlow("matem"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()
		val result = snapshot.searchResults.single()

		assertEquals(SyntheticTermSubjectAvailability.AVAILABLE, result.availability)
		assertNull(result.availabilityDetail)
	}

	private fun dataSource(): SyntheticTermCreationDataSource {
		val cacheKey = "2016-regular"
		val name = "Matemáticas II"
		return SyntheticTermCreationDataSource(
			academicRecordRepository = FakeAcademicRecordRepository(recordWithWithdrawnSubject(name)),
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

	private fun recordWithWithdrawnSubject(name: String): AcademicRecord {
		return AcademicRecord(
			id = "record",
			terms = listOf(
				AcademicTerm(
					id = "current",
					periodYear = 2025,
					periodCode = AcademicTermPeriod.APR_JUL,
					kind = TermKind.CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = "attempt-MA1112",
							subjectCode = "MA1112",
							subjectName = name,
							credits = 4,
							academicOutcome = AttemptOutcome.PENDING,
							withdrawn = true
						)
					)
				)
			)
		)
	}
}
