package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import kotlinx.datetime.LocalDate

object Schedule {
	sealed class State : ViewState {
		data object Idle : State()

		data object Loading : State()

		data class Content(
			// The term's name and, while the date is in one of its weeks, which one.
			val subtitle: UiText,
			val schedule: ScheduleItem,
			val viewMode: ScheduleViewMode
		) : State()

		// The record has no current term, or the current term has nothing scheduled.
		data object Empty : State()
	}

	sealed class Action : ViewAction {
		data object ObserveSchedule : Action()
		class SelectScheduleView(val viewMode: ScheduleViewMode) : Action()

		// The screen is back in front: the clock is read again instead of waiting for its next tick.
		data object Resumed : Action()

		// A cell marked with an evaluation was tapped: the Evaluations tab is asked for the week that
		// opens on [weekStart], the Monday of the calendar week the mark is about, and for the evaluation
		// [evaluationId] that the mark stands for.
		class EvaluationCellSelected(val weekStart: LocalDate, val evaluationId: String) : Action()
	}

	sealed class Effect : ViewEffect {
		// Take the student to the Evaluations tab, on the week that opens on [weekStart] and on the
		// evaluation [evaluationId].
		class NavigateToEvaluationsWeek(val weekStart: LocalDate, val evaluationId: String) : Effect()
	}
}
