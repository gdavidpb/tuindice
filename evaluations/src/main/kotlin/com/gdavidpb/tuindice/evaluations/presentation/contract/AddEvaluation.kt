package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object AddEvaluation {
	sealed class State : ViewState {
		object Loading : State()

		data class Content(
			val availableSubjects: List<Subject>,
			val subject: Subject? = null,
			val type: EvaluationType? = null,
			val date: Long? = null,
			val grade: Double? = null,
			val maxGrade: Double? = null
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadAvailableSubjects : Action()

		data class ClickDone(
			val subject: Subject?,
			val type: EvaluationType?,
			val date: Long?,
			val maxGrade: Double?
		) : Action()
	}

	sealed class Event : ViewEvent {
		class ShowSnackBar(
			val message: String
		) : Event()
	}
}