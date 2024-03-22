package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object Evaluation {
	sealed interface State : ViewState {
		data object Loading : State

		data class Content(
			val availableSubjects: List<Subject>,
			val selectedSubject: Subject? = null,
			val type: EvaluationType? = null,
			val date: Long? = null,
			val isOverdue: Boolean = false,
			val grade: Double? = null,
			val maxGrade: Double? = null
		) : State

		data object Failed : State
	}

	sealed interface Action : ViewAction {
		data object LoadAvailableSubjects : Action

		class LoadEvaluation(
			val evaluationId: String
		) : Action

		class SetSubject(
			val subject: Subject
		) : Action

		class SetType(
			val type: EvaluationType
		) : Action

		class SetDate(
			val date: Long?
		) : Action

		class SetGrade(
			val grade: Double
		) : Action

		class SetMaxGrade(
			val maxGrade: Double
		) : Action

		class ClickGrade(
			val grade: Double?,
			val maxGrade: Double?
		) : Action

		class ClickMaxGrade(
			val maxGrade: Double?
		) : Action

		class ClickAddEvaluation(
			val subject: Subject?,
			val type: EvaluationType?,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?
		) : Action

		class ClickEditEvaluation(
			val evaluationId: String,
			val subject: Subject?,
			val type: EvaluationType?,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?
		) : Action

		data object CloseDialog : Action
	}

	sealed interface Effect : ViewEffect {
		data object NavigateToEvaluations : Effect

		class ShowGradePickerDialog(
			val grade: Double?,
			val maxGrade: Double?
		) : Effect

		class ShowMaxGradePickerDialog(
			val maxGrade: Double?
		) : Effect

		class ShowSnackBar(
			val message: String
		) : Effect

		data object CloseDialog : Effect
	}
}