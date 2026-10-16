package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
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
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.testing.SleepingScheduleClockRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Summary is opened by two inputs that nothing orders: the observation the screen asks for and the
// RESUMED ResumedEffect fires for the screen's own birth. Neither order may read the clock twice.
class SummaryMachineResumeOrderTest {
	@Test
	fun theOpeningResume_restartsNothing_whenItArrivesBeforeTheObservation() = runTest {
		val world = World(this)

		world.process(Summary.Action.Resumed)
		world.process(Summary.Action.ObserveSummary)

		assertEquals(1, world.clock.observations)
	}

	@Test
	fun theOpeningResume_restartsNothing_whenItArrivesAfterTheObservation() = runTest {
		val world = World(this)

		world.process(Summary.Action.ObserveSummary)
		world.process(Summary.Action.Resumed)

		assertEquals(1, world.clock.observations)
	}

	@Test
	fun aReturn_afterTheOpening_readsTheClockAgain() = runTest {
		val world = World(this)

		world.process(Summary.Action.ObserveSummary)
		world.process(Summary.Action.Resumed)
		world.process(Summary.Action.Resumed)

		assertEquals(2, world.clock.observations)
	}

	@Test
	fun aReturn_readsTheClockOnce_howeverManyTimesTheScreenComesBack_andTheHubSeesTheNewMoment() = runTest {
		val world = World(this)

		world.process(Summary.Action.ObserveSummary)
		world.process(Summary.Action.Resumed)
		world.clock.reading = scheduleNow(minuteOfDay = 14 * 60)
		world.process(Summary.Action.Resumed)
		world.process(Summary.Action.Resumed)

		assertEquals(3, world.clock.observations)
		assertEquals(3, world.hubEvents.size)
	}

	@Test
	fun aReturnBeforeAnyObservation_doesNothing() = runTest {
		val world = World(this)

		world.process(Summary.Action.Resumed)
		world.process(Summary.Action.Resumed)

		assertEquals(0, world.clock.observations)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private class World(private val scope: TestScope) {
		val clock = SleepingScheduleClockRepository()
		val hubEvents = mutableListOf<SummaryHubItem>()
		private val reporting = RecordingReportingRepository()
		private val userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER))
		private val machine = SummaryMachine(
			observations = SummaryObservations(
				user = ObserveUserUseCase(userRepository, reporting),
				sync = ObserveSyncUseCase(FakeSyncStatusRepository(), FakeSyncRepository(), reporting),
				hub = createObserveSummaryHubUseCase(clock = clock)
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
		)
		private val host = object : MachineHost<Summary.Effect> {
			override fun sendEffect(effect: Summary.Effect) = Unit

			override fun processInternalEvent(event: Any) {
				if (event is SummaryInternalEvent.HubObserved) hubEvents += event.hub
			}

			override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
				return scope.backgroundScope.launch(block = block)
			}
		}
		private val definition: MachineDefinition<Summary.State> = machine.define(host)
		private var state: Summary.State = machine.initialState()

		suspend fun process(action: Summary.Action) {
			val result = definition.process(state, action)

			if (result is TransitionResult.Transitioned<Summary.State>) state = result.toState

			scope.runCurrent()
		}
	}
}
