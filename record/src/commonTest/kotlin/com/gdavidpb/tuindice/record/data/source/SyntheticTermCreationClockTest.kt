@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.testing.fixedClock
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

/** The term options start at the term the clock the data source was given says it is. */
class SyntheticTermCreationClockTest {
	@Test
	fun observeSnapshot_startsPeriodOptionsAtTheTermOfTheClockItIsGiven() = runTest {
		assertEquals(
			SyntheticTermPeriodOption(periodYear = 2026, periodCode = AcademicTermPeriod.SEP_DEC),
			firstPeriodOption(iso = "2026-10-15T12:00:00Z")
		)
		assertEquals(
			SyntheticTermPeriodOption(periodYear = 2027, periodCode = AcademicTermPeriod.JAN_MAR),
			firstPeriodOption(iso = "2027-01-10T12:00:00Z")
		)
	}

	private suspend fun firstPeriodOption(iso: String): SyntheticTermPeriodOption {
		val cacheKey = "2016-regular"
		val dataSource = SyntheticTermCreationDataSource(
			academicRecordRepository = FakeAcademicRecordRepository(AcademicRecord(id = "record")),
			caches = SyntheticTermCreationCaches(
				pensumCacheDao = FakePensumCacheDao(
					cache = PensumCacheEntity(
						cacheKey = cacheKey,
						year = 2016,
						modalityId = "regular",
						payloadJson = pensumPayload(nodes = emptyList(), edges = emptyList()),
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
				subjectCatalogCacheDao = FakeSubjectCatalogCacheDao(emptyList())
			),
			ktorClient = HttpClient(MockEngine { respondOk() }),
			json = Json { ignoreUnknownKeys = true },
			clock = fixedClock(iso)
		)

		return dataSource.observeSnapshot(
			queryFlow = MutableStateFlow(""),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first().periodOptions.first()
	}
}
