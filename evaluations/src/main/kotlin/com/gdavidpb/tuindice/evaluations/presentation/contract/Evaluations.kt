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

		data class EmptyMatch(
			val availableFilters: List<EvaluationFilter>,
			val activeFilters: List<EvaluationFilter>
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadEvaluations : Action()

		class FilterEvaluations(
			val filters: List<EvaluationFilter>
		) : Action()

		object AddEvaluation : Action()

		class EditEvaluation(
			val evaluationId: String
		) : Action()

		object OpenEvaluationsFilters : Action()

		object CloseDialog : Action()
	}

	sealed class Event : ViewEvent {
		class NavigateToAddEvaluation(
			val title: String
		) : Event()

		class NavigateToEvaluation(
			val title: String,
			val evaluationId: String
		) : Event()

		class ShowSnackBar(
			val message: String
		) : Event()

		class ShowFilterEvaluationsDialog(
			val availableFilters: List<EvaluationFilter>,
			val activeFilters: List<EvaluationFilter>
		) : Event()

		object CloseDialog : Event()
	}
}