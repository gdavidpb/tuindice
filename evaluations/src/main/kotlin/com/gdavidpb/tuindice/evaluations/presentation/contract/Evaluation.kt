package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object Evaluation {
	sealed class State : ViewState {
		object Loading : State()

		data class Content(
			val availableSubjects: List<Subject>,
			val subject: Subject? = null,
			val name: String? = null,
			val date: Long? = null,
			val grade: Double? = null,
			val maxGrade: Double? = null
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		class LoadEvaluation(
			val evaluationId: String
		) : Action()
	}

	sealed class Event : ViewEvent {
		class ShowSnackBar(
			val message: String
		) : Event()
	}
}