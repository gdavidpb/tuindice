package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.summary.domain.usecase.MarkTermReportSeenUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RateTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TermReportViewModelContractTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aReportIsOpened_then_itIsShownAndMarkedAsSeen() = runTest {
		withMainDispatcher { dispatchers ->
			val seen = ControllableTermReportSeenRepository()
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
				seen = seen
			)

			viewModel.openAction(insightId = "term-1:2")

			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<TermReport.State.Content>().first()
			}

			assertEquals("term-1:2", content.report.insightId)
			assertEquals("Abr - Jul 2026", content.report.periodLabel)
			assertTrue("term-1:2" in seen.seenIdsFlow.value)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aThumbIsTapped_then_itIsChosenAtOnce_andSentToTheService() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val viewModel = createViewModel(dispatchers = dispatchers, insights = insights)

			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }

			viewModel.rateAction(useful = true)

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(listOf("term-1:2" to true), insights.ratings)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theServiceRefusesAThumb_then_thePreviousThumbIsSelectedAgain() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ControllableAssistantInsightsRepository(
				initialInsights = listOf(termReportInsight(id = "term-1:2")),
				ratingFailure = IllegalStateException("404")
			)
			val viewModel = createViewModel(dispatchers = dispatchers, insights = insights)

			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }

			viewModel.rateAction(useful = false)

			assertNull(assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theServiceHasNoReportOfTheTerm_then_theScreenSaysSo() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers, insights = ControllableAssistantInsightsRepository())

			viewModel.openAction(insightId = "term-1:2")

			assertIs<TermReport.State.NoReport>(
				withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.NoReport>().first() }
			)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theReportCannotBeRead_then_retryReadsItAgain() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ControllableAssistantInsightsRepository(refreshFailure = IllegalStateException("offline"))
			val viewModel = createViewModel(dispatchers = dispatchers, insights = insights)

			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Failed>().first() }

			insights.refreshFailure = null
			insights.insightsFlow.value = listOf(termReportInsight(id = "term-1:2"))
			viewModel.retryAction()

			assertIs<TermReport.State.Content>(
				withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }
			)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aButtonOfWhatFollowsIsTapped_then_theScreenItNamesIsAsked() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			)
			val effects = mutableListOf<TermReport.Effect>()
			backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
				viewModel.effect.collect { effect -> effects += effect }
			}

			viewModel.openAction(insightId = "term-1:2")
			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<TermReport.State.Content>().first()
			}

			viewModel.openNextAction(content.report.nextActions.first())

			val effect = assertIs<TermReport.Effect.NavigateToAssistantTarget>(effects.single())
			assertEquals(AssistantNavTarget.RECORD_PROJECTION, effect.target)
			assertEquals(emptyList(), effect.entityRefs)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theShareActionIsTapped_then_theSheetOfTheTermIsAsked() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			)
			val effects = mutableListOf<TermReport.Effect>()
			backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
				viewModel.effect.collect { effect -> effects += effect }
			}

			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }

			viewModel.shareAction()

			assertEquals("2026-APR_JUL", assertIs<TermReport.Effect.NavigateToShareTerm>(effects.single()).termKey)
		}
	}

	private fun createViewModel(
		dispatchers: com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers,
		insights: ControllableAssistantInsightsRepository,
		seen: ControllableTermReportSeenRepository = ControllableTermReportSeenRepository()
	): TermReportViewModel {
		val reporting = RecordingReportingRepository()
		val closed = hubClosedTerm(
			year = 2026,
			attempts = listOf(hubPassedAttempt("CI1")),
			index = 4.0,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		// The report is about the term in progress, which the record holds beside the closed one.
		val current = hubTerm(
			attempts = listOf(hubPassedAttempt("CI3")),
			kind = TermKind.CURRENT,
			periodCode = AcademicTermPeriod.APR_JUL
		)
		val record = hubRecord(closed, current)

		return TermReportViewModel(
			screenMachine = TermReportMachine(
				observeTermReportUseCase = ObserveTermReportUseCase(
					insightsRepository = insights,
					visibleAcademicRecordRepository = ControllableVisibleAcademicRecordRepository(record),
					recordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
					reportingRepository = reporting
				),
				markTermReportSeenUseCase = MarkTermReportSeenUseCase(seen, reporting),
				rateTermReportUseCase = RateTermReportUseCase(insights, reporting),
				updateTermReportsUseCase = UpdateTermReportsUseCase(insights, reporting)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	private companion object {
		const val TIMEOUT_MILLIS = 5_000L
	}
}
