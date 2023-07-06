package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams

object Evaluation {
	sealed class State : ViewState {
		object Loading : State()

		data class Content(
			val availableSubjects: List<Subject>,
			val subject: Subject? = null,
			val name: String = "",
			val grade: Double = 0.0,
			val maxGrade: Double = 0.0
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		class LoadEvaluation(val params: GetEvaluationParams) : Action()
	}

	sealed class Event : ViewEvent {
		class ShowSnackBar(val message: String) : Event()
	}
}