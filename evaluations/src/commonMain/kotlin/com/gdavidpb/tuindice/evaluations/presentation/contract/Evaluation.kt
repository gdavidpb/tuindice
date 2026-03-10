package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object Evaluation {
	sealed class State : ViewState() {
		data object Loading : State()

		data class Content(
			val evaluationId: String? = null,
			override val topBarTitle: String =
				if (evaluationId != null)
					"Modificar evaluación"
				else
					"Agregar evaluación",
			override val isTopBarVisible: Boolean = true,
			val availableSubjects: List<Subject>,
			val selectedSubject: Subject? = null,
			val type: EvaluationType? = null,
			val scheduleMode: EvaluationScheduleMode = EvaluationScheduleMode.CONTINUOUS,
			val date: Long? = null,
			val isOverdue: Boolean = false,
			val grade: Double? = null,
			val maxGrade: Double? = null
		) : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		data object LoadAvailableSubjects : Action()

		class LoadEvaluation(
			val evaluationId: String
		) : Action()

		class SetSubject(
			val subject: Subject
		) : Action()

		class SetType(
			val type: EvaluationType
		) : Action()

		class SetDate(
			val date: Long?
		) : Action()

		class SetGrade(
			val grade: Double
		) : Action()

		class SetMaxGrade(
			val maxGrade: Double
		) : Action()

		class ClickGrade(
			val grade: Double?,
			val maxGrade: Double?
		) : Action()

		class ClickMaxGrade(
			val maxGrade: Double?
		) : Action()

		class ClickAddEvaluation(
			val subject: Subject?,
			val type: EvaluationType?,
			val scheduleMode: EvaluationScheduleMode,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?
		) : Action()

		class ClickEditEvaluation(
			val evaluationId: String,
			val subject: Subject?,
			val type: EvaluationType?,
			val scheduleMode: EvaluationScheduleMode,
			val date: Long?,
			val grade: Double?,
			val maxGrade: Double?
		) : Action()
	}

	sealed class Effect : ViewEffect() {
		data object NavigateToEvaluations : Effect()

		class NavigateToGradePickerDialog(
			val grade: Double?,
			val maxGrade: Double?
		) : Effect()

		class NavigateToMaxGradePickerDialog(
			val maxGrade: Double?
		) : Effect()

		class ShowSnackBar(
			val message: String
		) : Effect()
	}
}
