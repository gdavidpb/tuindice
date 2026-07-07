package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationAttemptPickerItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationRequiredField
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import kotlinx.datetime.LocalDate
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.top_bar_add_evaluation
import tuindice.evaluations.generated.resources.top_bar_edit_evaluation

object Evaluation {
	sealed class State : ViewState {
		data object Loading : State()

		data class Content(
			val evaluationId: String? = null,
			val initialDraft: Draft? = null,
			override val topBarTitle: UiText =
				if (evaluationId != null)
					UiText.Resource(Res.string.top_bar_edit_evaluation)
				else
					UiText.Resource(Res.string.top_bar_add_evaluation),
			override val isTopBarVisible: Boolean = true,
			val attemptItems: List<EvaluationAttemptPickerItem> = emptyList(),
			val selectedAttempt: EditableAttemptDescriptor? = null,
			val type: EvaluationType? = null,
			val typeItems: List<EvaluationTypePickerItem> = emptyList(),
			val scheduleMode: EvaluationScheduleMode = EvaluationScheduleMode.CONTINUOUS,
			val date: Long? = null,
			// Dates outside this range are disabled in the picker; null means unbounded
			// (no current term resolved).
			val selectableDateRange: ClosedRange<LocalDate>? = null,
			val isOverdue: Boolean = false,
			val grade: Double? = null,
			val maxGrade: Double? = null,
			val isSubmitting: Boolean = false,
			val missingFields: Set<EvaluationRequiredField> = emptySet(),
			val gradeSection: EvaluationGradeSectionItem = EvaluationGradeSectionItem(
				maxGradeTitleText = "",
				overdueTitleText = "",
				gradeText = "",
				maxGradeText = "",
				showsGradeChip = false
			)
		) : State() {
			val draft: Draft
				get() = Draft(
					attemptId = selectedAttempt?.id,
					type = type,
					scheduleMode = scheduleMode,
					date = date,
					grade = grade,
					maxGrade = maxGrade
				)

			val hasDraftChanges: Boolean
				get() = initialDraft?.let { draft -> draft != this.draft } ?: (evaluationId == null)

			val canSubmit: Boolean
				get() = !isSubmitting && hasDraftChanges

			val missingRequiredFields: Set<EvaluationRequiredField>
				get() = buildSet {
					if (selectedAttempt == null) add(EvaluationRequiredField.SUBJECT)
					if (type == null) add(EvaluationRequiredField.TYPE)
					if (maxGrade == null) add(EvaluationRequiredField.MAX_GRADE)
				}

			// An untouched add form is not worth a discard warning; in edit mode any
			// divergence from the loaded draft is.
			val hasDiscardableInput: Boolean
				get() = if (evaluationId == null) {
					selectedAttempt != null || type != null || date != null ||
						grade != null || maxGrade != null
				} else {
					hasDraftChanges
				}
		}

		data object Failed : State()
	}

	data class Draft(
		val attemptId: String?,
		val type: EvaluationType?,
		val scheduleMode: EvaluationScheduleMode,
		val date: Long?,
		val grade: Double?,
		val maxGrade: Double?
	)

	sealed class Action : ViewAction {
		data object LoadAvailableAttempts : Action()

		class LoadEvaluation(
			val evaluationId: String
		) : Action()

		class SetAttempt(
			val attempt: EditableAttemptDescriptor?
		) : Action()

		class SetType(
			val type: EvaluationType?
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
			val evaluationName: String,
			val subjectCode: String,
			val grade: Double?,
			val maxGrade: Double?
		) : Action()

		class ClickMaxGrade(
			val evaluationName: String,
			val subjectCode: String,
			val maxGrade: Double?
		) : Action()

		data object ClickSubmitEvaluation : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToEvaluations : Effect()

		class NavigateToGradePickerDialog(
			val evaluationName: String,
			val subjectCode: String,
			val grade: Double?,
			val maxGrade: Double?
		) : Effect()

		class NavigateToMaxGradePickerDialog(
			val evaluationName: String,
			val subjectCode: String,
			val maxGrade: Double?
		) : Effect()

		class ShowSnackBar(
			val message: String
		) : Effect()
	}
}
