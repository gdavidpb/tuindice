package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetTermKeyUseCaseTest {
	@Test
	fun execute_answersTheKeyOfTheTermWithThatId() = runTest {
		val repository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(id = "term-1", periodYear = 2023, periodCode = AcademicTermPeriod.SEP_DEC),
					academicTerm(id = "term-2", periodYear = 2024, periodCode = AcademicTermPeriod.JAN_MAR)
				)
			)
		)

		createUseCase(repository).execute("term-2").test {
			assertEquals("2024-${AcademicTermPeriod.JAN_MAR.name}", awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun execute_answersNull_whenTheRecordHasNoSuchTerm() = runTest {
		val repository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(id = "record", terms = listOf(academicTerm(id = "term-1")))
		)

		createUseCase(repository).execute("term-9").test {
			assertNull(awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun execute_answersNull_whenThereIsNoLocalRecord() = runTest {
		val repository = ControllableAcademicRecordRepository().apply { recordAvailable = false }

		createUseCase(repository).execute("term-1").test {
			assertNull(awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	private fun createUseCase(repository: ControllableAcademicRecordRepository) = GetTermKeyUseCase(
		academicRecordRepository = repository,
		reportingRepository = RecordingReportingRepository()
	)
}
