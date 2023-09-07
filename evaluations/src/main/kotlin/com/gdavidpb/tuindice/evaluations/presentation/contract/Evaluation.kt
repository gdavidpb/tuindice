package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError

object Evaluation {
	sealed class State : ViewState {
		object Loading : State()

		data class Content(
			val evaluationId: String? = null,
			val availableSubjects: List<Subject>,
			val subject: Subject? = null,
			val name: String? = null,
			val date: Long? = null,
			val grade: Double? = null,
			val maxGrade: Double? = null,
			val type: EvaluationType? = null,
			val exists: Boolean,
			val isCompleted: Boolean,
			val error: EvaluationError? = null
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		class LoadEvaluation(
			val evaluationId: String
		) : Action()

		class ClickSaveEvaluation(
			val evaluationId: String?,
			val subject: Subject?,
			val name: String?,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?,
			val type: EvaluationType?,
			val exists: Boolean,
			val isCompleted: Boolean
		) : Action()
	}

	sealed class Event : ViewEvent {
		class ShowSnackBar(
			val message: String
		) : Event()
	}
}