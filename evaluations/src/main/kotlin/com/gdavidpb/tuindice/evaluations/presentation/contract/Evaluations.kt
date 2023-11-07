package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

object Evaluations {
	sealed class State : ViewState {
		object Loading : State()

		data class Content(
			val originalEvaluations: List<Evaluation>,
			val filteredEvaluations: List<Evaluation>,
			val availableFilters: List<EvaluationFilter>,
			val activeFilters: List<EvaluationFilter>
		) : State()

		object Empty : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadEvaluations : Action()

		class CheckEvaluationFilter(
			val filter: EvaluationFilter
		) : Action()

		class UncheckEvaluationFilter(
			val filter: EvaluationFilter
		) : Action()

		object AddEvaluation : Action()

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

		object CloseDialog : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToAddEvaluation : Event()

		class NavigateToEvaluation(
			val evaluationId: String
		) : Event()

		class ShowGradePickerDialog(
			val evaluationId: String,
			val grade: Double,
			val maxGrade: Double
		) : Event()

		class ShowSnackBar(
			val message: String
		) : Event()

		object CloseDialog : Event()
	}
}