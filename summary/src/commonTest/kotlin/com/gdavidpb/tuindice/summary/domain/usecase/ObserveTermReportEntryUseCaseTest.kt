package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportEntry
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ObserveTermReportEntryUseCaseTest {

	@Test
	fun theNewestReportNotOpenedIsOffered_andItsLineGoesWhenOpened() = runTest {
		val seen = ControllableTermReportSeenRepository()
		val useCase = ObserveTermReportEntryUseCase(
			insightsRepository = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
			seenRepository = seen,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			awaitItem()

			assertEquals("term-1:2", entryOf(awaitItem())?.insightId)

			seen.markTermReportSeen("term-1:2")

			assertNull(entryOf(awaitItem()))
			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun entryOf(state: UseCaseState<SummaryTermReportEntry?, Nothing>): SummaryTermReportEntry? {
		return assertIs<UseCaseState.Data<SummaryTermReportEntry?>>(state).value
	}
}
