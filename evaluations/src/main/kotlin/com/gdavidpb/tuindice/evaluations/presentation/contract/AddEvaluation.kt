package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object AddEvaluation {
	sealed class State : ViewState {
		class Loading(
			val state: AddEvaluationStepState
		) : State()

		data class Step1(
			val availableSubjects: List<Subject> = emptyList(),
			val subject: Subject? = null,
			val type: EvaluationType? = null
		) : State(), AddEvaluationStepState

		data class Step2(
			val availableSubjects: List<Subject>,
			val subject: Subject,
			val type: EvaluationType,
			val date: Long? = null,
			val grade: Double? = null,
			val maxGrade: Double? = null
		) : State(), AddEvaluationStepState

		class Failed(
			val state: AddEvaluationStepState
		) : State()
	}

	sealed class Action : ViewAction {
		object LoadStep1 : Action()

		data class ClickStep1Done(
			val subject: Subject?,
			val type: EvaluationType?
		) : Action()

		object LoadStep2 : Action()

		data class ClickStep2Done(
			val subject: Subject,
			val type: EvaluationType,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?
		) : Action()
	}

	sealed class Event : ViewEvent {
		class NavigateTo(
			val subRoute: Destination.AddEvaluation
		) : Event()

		class ShowSnackBar(
			val message: String
		) : Event()
	}
}