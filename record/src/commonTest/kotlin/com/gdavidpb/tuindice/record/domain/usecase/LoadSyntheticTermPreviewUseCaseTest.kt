package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.LoadSyntheticTermPreviewParams
import com.gdavidpb.tuindice.record.testing.FakeSyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadSyntheticTermPreviewUseCaseTest {
	@Test
	fun execute_emitsPreviewFromRepository_forRequestedTermAndSubjects() = runTest {
		val preview = SyntheticTermLoadPreview(
			available = true,
			band = SyntheticTermLoadBand.NORMAL,
			credits = 12,
			loadIndex = 1.25
		)
		val repository = FakeSyntheticTermLoadPreviewRepository(preview = preview)
		val useCase = createUseCase(repository)

		useCase.execute(
			LoadSyntheticTermPreviewParams(
				termKey = "2027-SEP_DEC",
				subjectCodes = listOf("MA1112", "FS1111")
			)
		).test {
			assertEquals(preview, awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(
			listOf("2027-SEP_DEC" to listOf("MA1112", "FS1111")),
			repository.loadCalls
		)
	}

	@Test
	fun execute_emitsUnavailablePreview_withoutTransformingIt() = runTest {
		val repository = FakeSyntheticTermLoadPreviewRepository(
			preview = SyntheticTermLoadPreview(
				available = false,
				reason = "NOT_ENOUGH_HISTORY"
			)
		)
		val useCase = createUseCase(repository)

		useCase.execute(
			LoadSyntheticTermPreviewParams(
				termKey = "2027-SEP_DEC",
				subjectCodes = emptyList()
			)
		).test {
			val data = awaitLoadingThenData(this)
			assertEquals(false, data.available)
			assertEquals("NOT_ENOUGH_HISTORY", data.reason)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsTimeoutError_whenRepositoryTimesOut() = runTest {
		val repository = FakeSyntheticTermLoadPreviewRepository(
			throwable = IllegalStateException("Request timed out")
		)
		val useCase = createUseCase(repository)

		useCase.execute(
			LoadSyntheticTermPreviewParams(
				termKey = "2027-SEP_DEC",
				subjectCodes = listOf("MA1112")
			)
		).test {
			val error = awaitLoadingThenError(this)
			assertEquals(RecordUseCaseError.Timeout, error.error)

			awaitComplete()
		}
	}

	private fun createUseCase(
		repository: FakeSyntheticTermLoadPreviewRepository
	): LoadSyntheticTermPreviewUseCase {
		return LoadSyntheticTermPreviewUseCase(
			repository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)
	}
}
