package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSyncUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryObservations
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.ControllablePensumProgressRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.HUB_THURSDAY
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubMeeting
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.async
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
	private val record = hubRecord(
		hubTerm(
			attempts = listOf(
				hubAttempt(code = "CI5437", schedule = listOf(hubMeeting(day = HUB_THURSDAY, start = 3, end = 4)))
			)
		)
	)

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun theHub_reachesTheContent_andSurvivesTheUserBeingObservedAgain() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableStateFlow(DEFAULT_SUMMARY_USER)
			val viewModel = createViewModel(dispatchers = dispatchers, users = users)
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
			val viewModel = createViewModel(dispatchers = dispatchers, pensum = pensum)
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
	fun theHubIsOnTheStateBeforeTheUserIs_andTheContentTakesItWhenTheUserArrives() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableStateFlow(DEFAULT_SUMMARY_USER)
			val viewModel = createViewModel(dispatchers = dispatchers, users = users)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				val content = viewModel.awaitContent { true }

				// Whichever of the two observations spoke first, the content has both.
				assertEquals("Ana Diaz", content.name)
				assertEquals(viewModel.state.value.hub, content.hub)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun eachLineOfTheHub_asksForTheScreenThatOwnsIt() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers)
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
				val weekEffect = viewModel.effectAfter { openTargetAction(SummaryHubTarget.EvaluationsWeek(week)) }

				assertEquals(week, assertIs<Summary.Effect.NavigateToEvaluationsWeek>(weekEffect).weekStart)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun aTargetOfTheHub_leadsToTheRecord_orToTheWeekItNames() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				viewModel.awaitContent { true }

				val toRecord = viewModel.effectAfter { openTargetAction(SummaryHubTarget.Record) }
				val week = LocalDate(2026, 10, 5)
				val toWeek = viewModel.effectAfter {
					openTargetAction(SummaryHubTarget.EvaluationsWeek(weekStart = week))
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
			val viewModel = createViewModel(dispatchers = dispatchers, users = null)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				viewModel.openTargetAction(SummaryHubTarget.Record)

				assertNull(withTimeoutOrNullEffect(viewModel))
			} finally {
				collector.cancel()
			}
		}
	}

	private suspend fun SummaryViewModel.awaitContent(
		predicate: (Summary.State.Content) -> Boolean
	): Summary.State.Content {
		return withTimeout(3_000) {
			state.filterIsInstance<Summary.State.Content>().first { content -> predicate(content) }
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

	private fun createViewModel(
		dispatchers: TuIndiceDispatchers,
		users: MutableStateFlow<User>? = MutableStateFlow(DEFAULT_SUMMARY_USER),
		pensum: ControllablePensumProgressRepository = ControllablePensumProgressRepository()
	): SummaryViewModel {
		val userRepository = RecordingUserRepository(
			users = users ?: kotlinx.coroutines.flow.emptyFlow()
		)
		val reporting = RecordingReportingRepository()

		return SummaryViewModel(
			screenMachine = SummaryMachine(
				observations = SummaryObservations(
					user = ObserveUserUseCase(userRepository, reporting),
					sync = ObserveSyncUseCase(FakeSyncStatusRepository(), FakeSyncRepository(), reporting),
					hub = createObserveSummaryHubUseCase(
						record = ControllableVisibleAcademicRecordRepository(record),
						pensumProgress = pensum
					)
				),
				getCameraAvailabilityUseCase = GetCameraAvailabilityUseCase(FakeDeviceInfoRepository(), reporting),
				updateUserUseCase = UpdateUserUseCase(
					userRepository = userRepository,
					reportingRepository = reporting,
					exceptionHandler = UpdateUserExceptionHandler(FakeNetworkRepository(isAvailable = true))
				),
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = reporting,
					exceptionHandler = UploadProfilePictureExceptionHandler(FakeNetworkRepository(isAvailable = true))
				),
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = reporting,
					exceptionHandler = RemoveProfilePictureExceptionHandler(FakeNetworkRepository(isAvailable = true))
				)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}
}
