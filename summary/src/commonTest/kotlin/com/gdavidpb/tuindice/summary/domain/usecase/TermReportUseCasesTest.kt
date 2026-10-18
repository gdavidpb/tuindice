package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.usecase.param.RateTermReportParams
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TermReportUseCasesTest {

	@Test
	fun rateSendsTheThumbsOfTheStudent() = runTest {
		val insights = ControllableAssistantInsightsRepository()
		val useCase = RateTermReportUseCase(insights, RecordingReportingRepository())

		useCase.execute(RateTermReportParams(insightId = "term-1:2", useful = false)).test {
			awaitItem()
			assertIs<UseCaseState.Data<Unit>>(awaitItem())
			awaitComplete()
		}

		assertEquals(listOf("term-1:2" to false), insights.ratings)
	}

	@Test
	fun aRefusedRatingFailsTheUseCase() = runTest {
		val insights = ControllableAssistantInsightsRepository(ratingFailure = IOException("404"))
		val useCase = RateTermReportUseCase(insights, RecordingReportingRepository())

		useCase.execute(RateTermReportParams(insightId = "term-1:2", useful = true)).test {
			awaitItem()
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}
	}

	@Test
	fun markingAReportSeenKeepsItSeen() = runTest {
		val seen = ControllableTermReportSeenRepository()
		val useCase = MarkTermReportSeenUseCase(seen, RecordingReportingRepository())

		useCase.execute("term-1:2").test {
			awaitItem()
			awaitItem()
			awaitComplete()
		}

		assertEquals(setOf("term-1:2"), seen.seenIdsFlow.value)
	}

	@Test
	fun refreshingKeepsTheReportsWhenTheServiceIsOffline() = runTest {
		val insights = ControllableAssistantInsightsRepository(refreshFailure = IOException("offline"))
		val useCase = RefreshTermReportsUseCase(insights, RecordingReportingRepository())

		useCase.execute(Unit).test {
			awaitItem()
			assertIs<UseCaseState.Data<Unit>>(awaitItem())
			awaitComplete()
		}

		assertEquals(1, insights.refreshCount)
	}
}
