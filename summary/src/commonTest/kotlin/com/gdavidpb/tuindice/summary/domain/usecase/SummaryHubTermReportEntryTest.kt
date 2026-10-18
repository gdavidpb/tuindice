package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SummaryHubTermReportEntryTest {

	@Test
	fun theHubOffersTheNewestReportNotOpened_andDropsItOnceOpened() = runTest {
		val seen = ControllableTermReportSeenRepository()
		val useCase = createObserveSummaryHubUseCase(
			insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
			seenTermReports = seen
		)

		useCase.execute(Unit).test {
			assertEquals("term-1:2", awaitLoadingThenData(this).termReport?.insightId)

			seen.markTermReportSeen("term-1:2")

			assertNull(assertIs<UseCaseState.Data<SummaryHub>>(awaitItem()).value.termReport)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun theOfferDoesNotWaitForTheRecord() = runTest {
		val useCase = createObserveSummaryHubUseCase(
			insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
			prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = false)
		)

		useCase.execute(Unit).test {
			val hub = awaitLoadingThenData(this)

			assertNull(hub.week)
			assertEquals("term-1:2", hub.termReport?.insightId)
			cancelAndIgnoreRemainingEvents()
		}
	}
}
