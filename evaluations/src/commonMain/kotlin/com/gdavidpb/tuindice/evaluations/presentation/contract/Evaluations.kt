package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.resource.evaluationsScreenTitle
import kotlinx.coroutines.flow.Flow

object Evaluations {
	sealed class State(
		override val topBarTitle: String = "Evaluaciones",
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState() {
		data object Loading : State()

		data class Content(
			val originalEvaluations: List<Evaluation>,
			val filteredEvaluations: List<Evaluation>,
			val availableFilters: List<EvaluationFilter>,
			val activeFilters: List<EvaluationFilter>
		) : State()

		data object Empty : State()

		data object NoSubjects : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		class LoadEvaluations(
			val activeFilters: Flow<List<EvaluationFilter>>
		) : Action()

		class CheckEvaluationFilter(
			val filter: EvaluationFilter
		) : Action()

		class UncheckEvaluationFilter(
			val filter: EvaluationFilter
		) : Action()

		data object ClearEvaluationFilters : Action()

		data object AddEvaluation : Action()

		class ShowEvaluationGradeDialog(
			val evaluationId: String
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
			val grade: Double,
			val maxGrade: Double
		) : Effect()

		class ShowSnackBar(
			val message: String
		) : Effect()
	}
}
