package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlowUseCaseTest {
	@Test
	fun when_exceptionHandlerIsMissing_then_flowUseCaseStillReports() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val throwable = IllegalStateException("boom", IllegalArgumentException("root"))
		val useCase = TestFallbackUseCase(
			reportingRepository = reportingRepository,
			throwable = throwable
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertLoggedThrowable(throwable, reportingRepository.loggedExceptions.single())
		assertEquals("TestFallbackUseCase", reportingRepository.customKeys["use-case"])
		assertEquals(false, reportingRepository.customKeys["is-handled"])
		assertEquals("IllegalStateException", reportingRepository.customKeys["throwable-class"])
		assertEquals("boom", reportingRepository.customKeys["throwable-message"])
		assertEquals("IllegalArgumentException", reportingRepository.customKeys["root-cause-class"])
		assertEquals("root", reportingRepository.customKeys["root-cause-message"])
		assertEquals("none", reportingRepository.customKeys["error-class"])
	}

	@Test
	fun when_exceptionHandlerExists_then_flowUseCaseReportsAndMarksHandled() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val throwable = IllegalStateException("boom")
		val useCase = TestHandledUseCase(
			reportingRepository = reportingRepository,
			exceptionHandler = TestExceptionHandler(),
			throwable = throwable
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertLoggedThrowable(throwable, reportingRepository.loggedExceptions.single())
		assertEquals("TestHandledUseCase", reportingRepository.customKeys["use-case"])
		assertEquals(true, reportingRepository.customKeys["is-handled"])
		assertEquals("IllegalStateException", reportingRepository.customKeys["throwable-class"])
		assertEquals("boom", reportingRepository.customKeys["throwable-message"])
		assertEquals("IllegalStateException", reportingRepository.customKeys["root-cause-class"])
		assertEquals("boom", reportingRepository.customKeys["root-cause-message"])
		assertEquals("Handled", reportingRepository.customKeys["error-class"])
		assertEquals(TestUseCaseError.Handled, (states.last() as UseCaseState.Error).error)
	}

	@Test
	fun when_flowUseCaseIsCancelled_then_itDoesNotReport() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val useCase = TestFallbackUseCase(
			reportingRepository = reportingRepository,
			throwable = CancellationException("cancelled")
		)

		assertFailsWith<CancellationException> {
			useCase.execute(Unit).toList()
		}

		assertEquals(emptyList(), reportingRepository.loggedExceptions)
	}

	private class TestFallbackUseCase(
		override val reportingRepository: ReportingRepository,
		private val throwable: Throwable
	) : FlowUseCase<Unit, Unit, Nothing>(reportingRepository = reportingRepository) {
		override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
			throw throwable
		}
	}

	private class TestHandledUseCase(
		override val reportingRepository: ReportingRepository,
		override val exceptionHandler: TestExceptionHandler,
		private val throwable: Throwable
	) : FlowUseCase<Unit, Unit, TestUseCaseError>(reportingRepository = reportingRepository) {
		override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
			throw throwable
		}
	}

	private class TestExceptionHandler : ExceptionHandler<TestUseCaseError>() {
		override fun parseException(throwable: Throwable): TestUseCaseError {
			return TestUseCaseError.Handled
		}
	}

	private fun assertLoggedThrowable(expected: Throwable, actual: Throwable) {
		assertEquals(expected::class, actual::class)
		assertEquals(expected.message, actual.message)
	}

	private sealed interface TestUseCaseError : UseCaseError {
		data object Handled : TestUseCaseError
	}
}
