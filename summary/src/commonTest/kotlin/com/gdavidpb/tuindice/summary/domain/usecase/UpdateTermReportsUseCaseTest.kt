package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
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
		val useCase = UpdateTermReportsUseCase(
			insightsRepository = insights,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertEquals(true, assertIs<UseCaseState.Data<Boolean>>(awaitItem()).value)
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
		val useCase = UpdateTermReportsUseCase(
			insightsRepository = insights,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			awaitItem()
			assertEquals(false, assertIs<UseCaseState.Data<Boolean>>(awaitItem()).value)
			awaitComplete()
		}

		assertEquals(listOf("term-1:2"), insights.insightsFlow.value.map { insight -> insight.id })
	}
}
