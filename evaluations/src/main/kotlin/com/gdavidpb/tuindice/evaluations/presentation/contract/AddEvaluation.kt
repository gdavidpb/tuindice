package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object AddEvaluation {
	sealed class State : ViewState {
		object Loading : State()

		data class Step1(
			val availableSubjects: List<Subject>,
			val subject: Subject? = null,
			val type: EvaluationType? = null
		) : State()

		data class Step2(
			val subject: Subject,
			val type: EvaluationType,
			val date: Long? = null,
			val grade: Double? = null,
			val maxGrade: Double? = null
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadAvailableSubjects : Action()

		data class ClickStep1Done(
			val subject: Subject?,
			val type: EvaluationType?
		) : Action()

		data class ClickStep2Done(
			val subject: Subject,
			val type: EvaluationType,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?
		) : Action()
	}

	sealed class Event : ViewEvent {
		class NavigateToStep1(
			val subject: Subject,
			val type: EvaluationType
		) : Event()

		class NavigateToStep2(
			val subject: Subject,
			val type: EvaluationType
		) : Event()

		class ShowSnackBar(
			val message: String
		) : Event()
	}
}