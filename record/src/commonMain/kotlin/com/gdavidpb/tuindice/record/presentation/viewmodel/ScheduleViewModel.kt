package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine

class ScheduleViewModel(
	override val screenMachine: ScheduleMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Schedule.State, Schedule.Action, Schedule.Effect>(
	name = "schedule",
	initialState = screenMachine.initialState(),
	initialAction = Schedule.Action.ObserveSchedule,
	dispatchers = dispatchers
) {
	fun selectScheduleViewAction(viewMode: ScheduleViewMode) {
		sendAction(Schedule.Action.SelectScheduleView(viewMode))
	}
}
