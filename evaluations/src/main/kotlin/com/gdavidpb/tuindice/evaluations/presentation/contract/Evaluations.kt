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
			val evaluations: List<Evaluation>
		) : State()

		object Empty : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		class LoadEvaluation(
			val filters: List<EvaluationFilter>
		) : Action()

		object AddEvaluation : Action()

		object OpenEvaluationsFilters : Action()

		object CloseDialog : Action()
	}

	sealed class Event : ViewEvent {
		class ShowSnackBar(
			val message: String
		) : Event()

		class ShowFilterEvaluationsDialog(
			val filters: List<EvaluationFilter>
		) : Event()

		object CloseDialog : Event()
	}
}