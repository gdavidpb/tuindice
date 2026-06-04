package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsTab
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import kotlinx.coroutines.flow.Flow
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.screen_title_evaluations

object Evaluations {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.screen_title_evaluations),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState() {
		data object Idle : State()

		data object Loading : State()

		data class Content(
			val selectedTab: EvaluationsTab,
			val upcomingGroups: List<EvaluationsGroupItem>,
			val historyGroups: List<EvaluationsGroupItem>,
			val weekItem: EvaluationsWeekItem,
			val weekItems: List<EvaluationsWeekItem> = listOf(weekItem),
			val selectedWeekNumber: Int = weekItem.weekNumber,
			val weeklyUpcomingGroups: Map<Int, List<EvaluationsGroupItem>> = mapOf(selectedWeekNumber to upcomingGroups),
			val weeklyHistoryGroups: Map<Int, List<EvaluationsGroupItem>> = mapOf(selectedWeekNumber to historyGroups),
			val upcomingWeekGroups: List<EvaluationsWeekGroupItem> = listOf(
				EvaluationsWeekGroupItem(
					weekNumber = selectedWeekNumber,
					title = weekItem.labelText,
					groups = upcomingGroups
				)
			),
			val historyWeekGroups: List<EvaluationsWeekGroupItem> = listOf(
				EvaluationsWeekGroupItem(
					weekNumber = selectedWeekNumber,
					title = weekItem.labelText,
					groups = historyGroups
				)
			),
			val evaluationWeekGroups: List<EvaluationsWeekGroupItem> = when (selectedTab) {
				EvaluationsTab.Upcoming -> upcomingWeekGroups
				EvaluationsTab.History -> historyWeekGroups
			},
			val evaluationGroups: List<EvaluationsGroupItem>,
			val filterGroups: List<EvaluationFilterGroupItem>,
			val activeFilters: List<EvaluationFilter>
		) : State() {
			val hasActiveFilters: Boolean
				get() = activeFilters.isNotEmpty()
		}

		data object Empty : State()

		data object NoAttempts : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		class LoadEvaluations(
			val activeFilters: Flow<List<EvaluationFilter>>
		) : Action()

		data object RefreshEvaluations : Action()

		class CheckEvaluationFilter(
			val filter: EvaluationFilter
		) : Action()

		class UncheckEvaluationFilter(
			val filter: EvaluationFilter
		) : Action()

		data object ClearEvaluationFilters : Action()

		class SelectTab(
			val tab: EvaluationsTab
		) : Action()

		class SelectWeek(
			val weekNumber: Int
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

	sealed class Effect : ViewEffect() {
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
