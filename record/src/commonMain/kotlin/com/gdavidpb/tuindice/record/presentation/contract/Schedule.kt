package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem

object Schedule {
	sealed class State : ViewState {
		data object Idle : State()

		data object Loading : State()

		data class Content(
			val termName: String,
			val schedule: ScheduleItem,
			val viewMode: ScheduleViewMode
		) : State()

		// The record has no current term, or the current term has nothing scheduled.
		data object Empty : State()
	}

	sealed class Action : ViewAction {
		data object ObserveSchedule : Action()
		class SelectScheduleView(val viewMode: ScheduleViewMode) : Action()
	}

	sealed class Effect : ViewEffect
}
