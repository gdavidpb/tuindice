package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateTermReportsExceptionHandler
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateTermReportsUseCaseTest {

	@Test
	fun theReportsAreAskedOfTheServiceAndTheUseCaseSaysSo() = runTest {
		val insights = ControllableAssistantInsightsRepository()
		val useCase = createUseCase(insights)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertEquals(Unit, assertIs<UseCaseState.Data<Unit>>(awaitItem()).value)
			awaitComplete()
		}

		assertEquals(1, insights.refreshCount)
	}

	@Test
	fun aFailedRefreshKeepsTheReportsAlreadyRead_andSaysSo() = runTest {
		val insights = ControllableAssistantInsightsRepository(
			initialInsights = listOf(termReportInsight(id = "term-1:2")),
			refreshFailure = IllegalStateException("offline")
		)
		val useCase = createUseCase(insights)

		useCase.execute(Unit).test {
			awaitItem()
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}

		assertEquals(listOf("term-1:2"), insights.insightsFlow.value.map { insight -> insight.id })
	}

	@Test
	fun aRefreshWithoutConnectionIsAControlledError() = runTest {
		val useCase = createUseCase(
			ControllableAssistantInsightsRepository(refreshFailure = IllegalStateException("network is unreachable"))
		)

		useCase.execute(Unit).test {
			awaitItem()
			assertEquals(
				UpdateTermReportsUseCaseError.NoConnection,
				assertIs<UseCaseState.Error<UpdateTermReportsUseCaseError>>(awaitItem()).error
			)
			awaitComplete()
		}
	}

	private fun createUseCase(insights: ControllableAssistantInsightsRepository) = UpdateTermReportsUseCase(
		insightsRepository = insights,
		reportingRepository = RecordingReportingRepository(),
		exceptionHandler = UpdateTermReportsExceptionHandler()
	)
}
