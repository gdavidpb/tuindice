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
			val isOverdue: Boolean = false,
			val grade: Double? = null,
			val maxGrade: Double? = null
		) : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadAvailableSubjects : Action()

		data class ClickGrade(
			val grade: Double
		) : Action()

		data class ClickMaxGrade(
			val grade: Double
		) : Action()

		data class ClickDone(
			val subject: Subject?,
			val type: EvaluationType?,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?
		) : Action()

		object CloseDialog : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToEvaluations : Event()

		class ShowGradePickerDialog(
			val grade: Double
		) : Event()

		class ShowMaxGradePickerDialog(
			val grade: Double
		) : Event()

		class ShowSnackBar(
			val message: String
		) : Event()

		object CloseDialog : Event()
	}
}