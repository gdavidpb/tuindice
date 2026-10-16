package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.record.domain.usecase.ObserveScheduleUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetScheduleViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.ControllableVisibleEvaluationsRepository
import com.gdavidpb.tuindice.record.testing.RecordingScheduleSelectionRepository
import com.gdavidpb.tuindice.record.testing.SleepingScheduleClockRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The sheet is opened by two inputs that nothing orders: the observation the screen asks for and the
// RESUMED ResumedEffect fires for the sheet's own birth. Neither order may read the clock twice.
class ScheduleMachineResumeOrderTest {
	@Test
	fun theOpeningResume_restartsNothing_whenItArrivesBeforeTheObservation() = runTest {
		val world = World(this)

		world.process(Schedule.Action.Resumed)
		world.process(Schedule.Action.ObserveSchedule)

		assertEquals(1, world.clock.observations)
	}

	@Test
	fun theOpeningResume_restartsNothing_whenItArrivesAfterTheObservation() = runTest {
		val world = World(this)

		world.process(Schedule.Action.ObserveSchedule)
		world.process(Schedule.Action.Resumed)

		assertEquals(1, world.clock.observations)
	}

	@Test
	fun aReturn_afterTheOpening_readsTheClockAgain() = runTest {
		val world = World(this)

		world.process(Schedule.Action.ObserveSchedule)
		world.process(Schedule.Action.Resumed)
		world.process(Schedule.Action.Resumed)

		assertEquals(2, world.clock.observations)
	}

	private class World(private val scope: TestScope) {
		val clock = SleepingScheduleClockRepository()
		private val selection = RecordingScheduleSelectionRepository()
		private val reporting = RecordingReportingRepository()
		private val machine = ScheduleMachine(
			observeScheduleUseCase = ObserveScheduleUseCase(
				academicRecordRepository = ControllableAcademicRecordRepository(
					initialRecord = AcademicRecord(id = "record"),
					initialHasSynced = true
				),
				scheduleSelectionRepository = selection,
				scheduleClockRepository = clock,
				visibleEvaluationsRepository = ControllableVisibleEvaluationsRepository(),
				reportingRepository = reporting
			),
			setScheduleViewModeUseCase = SetScheduleViewModeUseCase(
				scheduleSelectionRepository = selection,
				reportingRepository = reporting
			)
		)
		private val host = object : MachineHost<Schedule.Effect> {
			override fun sendEffect(effect: Schedule.Effect) = Unit

			override fun processInternalEvent(event: Any) = Unit

			override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
				return scope.backgroundScope.launch(block = block)
			}
		}
		private val definition: MachineDefinition<Schedule.State> = machine.define(host)
		private var state: Schedule.State = machine.initialState()

		suspend fun process(action: Schedule.Action) {
			val result = definition.process(state, action)

			if (result is TransitionResult.Transitioned<Schedule.State>) state = result.toState

			scope.runCurrent()
		}
	}
}
