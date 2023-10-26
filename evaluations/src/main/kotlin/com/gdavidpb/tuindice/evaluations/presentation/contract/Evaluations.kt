package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem

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

		class CheckEvaluationAsCompleted(
			val evaluation: EvaluationItem
		) : Action()

		class UncheckEvaluationAsCompleted(
			val evaluation: EvaluationItem
		) : Action()

		object AddEvaluation : Action()

		class EditEvaluation(
			val evaluation: EvaluationItem
		) : Action()

		class RemoveEvaluation(
			val evaluation: EvaluationItem
		) : Action()

		class ConfirmRemoveEvaluation(
			val evaluation: EvaluationItem
		) : Action()

		object CloseDialog : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToAddEvaluation : Event()

		class NavigateToEvaluation(
			val evaluation: EvaluationItem
		) : Event()

		class ShowRemoveEvaluationDialog(
			val evaluation: EvaluationItem
		) : Event()

		object CloseDialog : Event()

		class ShowSnackBar(
			val message: String
		) : Event()
	}
}