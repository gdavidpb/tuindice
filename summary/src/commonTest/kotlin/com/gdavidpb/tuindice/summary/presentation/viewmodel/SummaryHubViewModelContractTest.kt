package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.ControllablePensumProgressRepository
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.awaitContent
import com.gdavidpb.tuindice.summary.testing.awaitHubOutsideTheContent
import com.gdavidpb.tuindice.summary.testing.createHubScenarioViewModel
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SummaryHubViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun theHub_reachesTheContent_andSurvivesTheUserBeingObservedAgain() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableStateFlow(DEFAULT_SUMMARY_USER)
			val viewModel = createHubScenarioViewModel(dispatchers = dispatchers, users = users)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				val first = viewModel.awaitContent { content -> content.hub.week != null }

				assertNotNull(first.hub.week?.classRow)
				assertEquals(SummaryCareerItem.Invite, first.hub.career)

				// A new user rebuilds the content: the hub, which the user knows nothing of, stays.
				users.value = DEFAULT_SUMMARY_USER.copy(grade = 3.0)

				val rebuilt = viewModel.awaitContent { content -> content.grade == 3.0f }

				assertEquals(first.hub, rebuilt.hub)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun theHub_followsTheSourcesWhileTheContentStays() = runTest {
		withMainDispatcher { dispatchers ->
			val pensum = ControllablePensumProgressRepository()
			val viewModel = createHubScenarioViewModel(dispatchers = dispatchers, pensum = pensum)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				viewModel.awaitContent { content -> content.hub.career == SummaryCareerItem.Invite }

				pensum.progressFlow.value = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)

				val progress = viewModel.awaitContent { content -> content.hub.career is SummaryCareerItem.Progress }

				assertEquals("156", (progress.hub.career as SummaryCareerItem.Progress).valueText)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun theHubThatArrivesBeforeTheUser_isTheOneOfTheContentWhenTheUserArrives() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableSharedFlow<User>()
			val viewModel = createHubScenarioViewModel(dispatchers = dispatchers, users = users)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				val beforeTheUser = viewModel.awaitHubOutsideTheContent()

				users.emit(DEFAULT_SUMMARY_USER)

				val content = viewModel.awaitContent { true }

				assertEquals("Ana Diaz", content.name)
				assertNotNull(content.hub.week)
				assertEquals(beforeTheUser, content.hub)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun theHubThatArrivesBeforeTheUser_survivesTheRefreshThatIsRunning() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableSharedFlow<User>()
			val refreshGate = CompletableDeferred<Unit>()
			val viewModel = createHubScenarioViewModel(dispatchers = dispatchers, users = users, refreshGate = refreshGate)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				val beforeTheUser = viewModel.awaitHubOutsideTheContent()

				viewModel.refreshSummaryAction()

				val loading = withTimeout(3_000) {
					viewModel.state.filterIsInstance<Summary.State.Loading>().first()
				}

				assertEquals(beforeTheUser, loading.hub)

				refreshGate.complete(Unit)
				users.emit(DEFAULT_SUMMARY_USER)

				assertEquals(beforeTheUser, viewModel.awaitContent { true }.hub)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun eachLineOfTheHub_asksForTheScreenThatOwnsIt() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createHubScenarioViewModel(dispatchers = dispatchers)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				viewModel.awaitContent { content -> content.hub.week != null }

				assertEquals(
					Summary.Effect.NavigateToSchedule,
					viewModel.effectAfter { openTargetAction(SummaryHubTarget.Schedule) }
				)
				assertEquals(
					Summary.Effect.NavigateToRecord,
					viewModel.effectAfter { openTargetAction(SummaryHubTarget.Record) }
				)
				assertEquals(
					Summary.Effect.NavigateToPensum,
					viewModel.effectAfter { openTargetAction(SummaryHubTarget.Pensum) }
				)

				val week = LocalDate(2026, 10, 12)
				val weekEffect = viewModel.effectAfter {
					openTargetAction(SummaryHubTarget.EvaluationsWeek(week, evaluationId = "evaluation-1"))
				}

				val toWeekEffect = assertIs<Summary.Effect.NavigateToEvaluationsWeek>(weekEffect)
				assertEquals(week, toWeekEffect.weekStart)
				assertEquals("evaluation-1", toWeekEffect.evaluationId)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun aTargetOfTheHub_leadsToTheRecord_orToTheWeekItNames() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createHubScenarioViewModel(dispatchers = dispatchers)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				viewModel.awaitContent { true }

				val toRecord = viewModel.effectAfter { openTargetAction(SummaryHubTarget.Record) }
				val week = LocalDate(2026, 10, 5)
				val toWeek = viewModel.effectAfter {
					openTargetAction(SummaryHubTarget.EvaluationsWeek(weekStart = week, evaluationId = "evaluation-1"))
				}

				assertEquals(Summary.Effect.NavigateToRecord, toRecord)
				assertEquals(week, assertIs<Summary.Effect.NavigateToEvaluationsWeek>(toWeek).weekStart)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun withoutContentYet_aLineOfTheHubLeadsNowhere() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createHubScenarioViewModel(dispatchers = dispatchers, users = null)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				viewModel.openTargetAction(SummaryHubTarget.Record)

				assertNull(withTimeoutOrNullEffect(viewModel))
			} finally {
				collector.cancel()
			}
		}
	}

	private suspend fun SummaryViewModel.effectAfter(action: SummaryViewModel.() -> Unit): Summary.Effect {
		return kotlinx.coroutines.coroutineScope {
			val effect = async { this@effectAfter.effect.first() }

			action()

			withTimeout(3_000) { effect.await() }
		}
	}

	private suspend fun withTimeoutOrNullEffect(viewModel: SummaryViewModel): Summary.Effect? {
		return kotlinx.coroutines.withTimeoutOrNull(300) { viewModel.effect.first() }
	}
}
