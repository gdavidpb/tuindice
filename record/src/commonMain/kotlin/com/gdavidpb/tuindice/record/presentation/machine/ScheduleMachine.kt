package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.SyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.model.resolveSyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.record.domain.model.ObservedSchedule
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.domain.usecase.ObserveScheduleUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetScheduleViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.mapper.toScheduleItem
import com.gdavidpb.tuindice.record.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.record.presentation.transition.scheduleTransitions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

class ScheduleMachine(
	private val observeScheduleUseCase: ObserveScheduleUseCase,
	private val setScheduleViewModeUseCase: SetScheduleViewModeUseCase
) : ScreenMachine<Schedule.State, Schedule.Effect> {
	// The observation is an any-state row and launchMachineJob accumulates rather than replaces:
	// a second dispatch would leave two observers feeding the same machine.
	private var scheduleObservationJob: Job? = null

	override fun initialState(): Schedule.State = Schedule.State.Idle

	override fun define(host: MachineHost<Schedule.Effect>): MachineDefinition<Schedule.State> {
		return MachineDefinition.define {
			scheduleTransitions(machine = this@ScheduleMachine, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<Schedule.Effect>) {
		if (scheduleObservationJob?.isActive == true) return

		scheduleObservationJob = host.launchMachineJob {
			observeScheduleUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						useCaseState.value.toInternalEvent()
					)

					// The record could not be read: there is nothing to draw, which is what Empty says.
					is UseCaseState.Error -> host.processInternalEvent(
						ScheduleInternalEvent.ScheduleEmptyObserved
					)
				}
			}
		}
	}

	internal fun setViewMode(host: MachineHost<Schedule.Effect>, viewMode: ScheduleViewMode) {
		host.launchMachineJob {
			setScheduleViewModeUseCase.execute(viewMode).collect()
		}
	}

	private fun ObservedSchedule.toInternalEvent(): ScheduleInternalEvent {
		val term = currentTerm
		val schedule = term?.attempts?.toScheduleItem()

		return if (term != null && schedule != null) {
			ScheduleInternalEvent.ScheduleContentObserved(
				termName = term.toShortNameText(),
				schedule = schedule,
				viewMode = viewMode
			)
		} else {
			when (
				resolveSyncedContentResolution(
					hasContent = false,
					hasSynced = hasSyncedRecord,
					keepCurrentWhileWaiting = false
				)
			) {
				SyncedContentResolution.Empty -> ScheduleInternalEvent.ScheduleEmptyObserved

				SyncedContentResolution.Content,
				SyncedContentResolution.Loading,
				SyncedContentResolution.KeepCurrent,
				-> ScheduleInternalEvent.ScheduleWaitingObserved
			}
		}
	}
}
