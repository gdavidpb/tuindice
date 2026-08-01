package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.screen_title_evaluations

object Evaluations {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.screen_title_evaluations),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState {
		data object Idle : State()

		data object Loading : State()

		data class Content(
			val weekItem: EvaluationsWeekItem,
			val weekItems: List<EvaluationsWeekItem> = listOf(weekItem),
			val selectedWeekKey: EvaluationsWeekKey = weekItem.key,
			val evaluationGroups: List<EvaluationsGroupItem>,
			val evaluationWeekGroups: List<EvaluationsWeekGroupItem> = listOf(
				EvaluationsWeekGroupItem(
					key = selectedWeekKey,
					title = weekItem.labelText,
					groups = evaluationGroups
				)
			)
		) : State()

		data object Empty : State()

		data class NoAttempts(
			val reason: EvaluationsNoAttemptsReason
		) : State()

		// Split from Failed because it is not the user's to retry: it means the record
		// sync itself is failing, and only a sync can clear it. Mirrors Pensum, which
		// already models this cause as its own state.
		data object RecordDataUnavailable : State()

		data class Failed(
			val message: String
		) : State()
	}

	sealed class Action : ViewAction {
		data object LoadEvaluations : Action()

		data object EnsureEvaluationsLoaded : Action()

		data object RefreshEvaluations : Action()

		class SelectWeek(
			val weekKey: EvaluationsWeekKey
		) : Action()

		data object AddEvaluation : Action()

		class ShowEvaluationGradeDialog(
			val evaluationId: String,
			val evaluationName: String,
			val subjectCode: String
		) : Action()

		class SetEvaluationGrade(
			val evaluationId: String,
			val grade: Double
		) : Action()

		class EditEvaluation(
			val evaluationId: String
		) : Action()

		class RemoveEvaluation(
			val evaluationId: String
		) : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToAddEvaluation : Effect()

		class NavigateToEvaluation(
			val evaluationId: String
		) : Effect()

		class NavigateToGradePickerDialog(
			val evaluationId: String,
			val evaluationName: String,
			val subjectCode: String,
			val grade: Double,
			val maxGrade: Double
		) : Effect()

		class ShowSnackBar(
			val message: String
		) : Effect()
	}
}
