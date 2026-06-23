package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentLoadResult
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EnsureRecordLoadedUseCaseTest {
	@Test
	fun execute_whenLocalContentExists_emitsCachedThenRefreshLifecycle() = runTest {
		val repository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(
				id = "record",
				terms = listOf(academicTerm(id = "term"))
			),
			initialHasSynced = true
		)
		val useCase = createUseCase(repository)

		useCase.execute(Unit).test {
			assertEquals(InitialContentLoadResult.Cached, awaitLoadingThenData(this))
			assertEquals(
				InitialContentLoadResult.RefreshStarted,
				assertIs<UseCaseState.Data<InitialContentLoadResult>>(awaitItem()).value
			)
			assertEquals(
				InitialContentLoadResult.RefreshSucceeded,
				assertIs<UseCaseState.Data<InitialContentLoadResult>>(awaitItem()).value
			)
			awaitComplete()
		}

		assertEquals(1, repository.updateAcademicRecordCalls)
		assertEquals(listOf(false), repository.updateAcademicRecordForceRemoteCalls)
	}

	@Test
	fun execute_whenLocalContentIsMissing_refreshesWithoutCachedEmission() = runTest {
		val repository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(id = "record"),
			initialHasSynced = false
		)
		val useCase = createUseCase(repository)

		useCase.execute(Unit).test {
			assertEquals(InitialContentLoadResult.RefreshStarted, awaitLoadingThenData(this))
			assertEquals(
				InitialContentLoadResult.RefreshSucceeded,
				assertIs<UseCaseState.Data<InitialContentLoadResult>>(awaitItem()).value
			)
			awaitComplete()
		}

		assertEquals(1, repository.updateAcademicRecordCalls)
		assertEquals(listOf(true), repository.updateAcademicRecordForceRemoteCalls)
	}

	private fun createUseCase(
		repository: ControllableAcademicRecordRepository
	): EnsureRecordLoadedUseCase {
		return EnsureRecordLoadedUseCase(
			academicRecordRepository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)
	}
}
