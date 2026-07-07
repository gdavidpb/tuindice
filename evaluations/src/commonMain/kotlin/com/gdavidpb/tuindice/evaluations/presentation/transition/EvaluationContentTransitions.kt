package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationInternalEvent
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationMachine
import com.gdavidpb.tuindice.evaluations.presentation.mapper.updated
import com.gdavidpb.tuindice.evaluations.presentation.mapper.withSelectedAttempt
import com.gdavidpb.tuindice.evaluations.presentation.mapper.withSelectedType
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationRequiredField
import com.gdavidpb.tuindice.evaluations.presentation.utils.isDateInPast
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE

internal fun MachineDefinitionBuilder<Evaluation.State>.evaluationContentTransitions(
	machine: EvaluationMachine,
	host: MachineHost<Evaluation.Effect>
) {
	from<Evaluation.State.Content> {
		on<Evaluation.Action.SetAttempt> { state, action ->
			state.copy(
				selectedAttempt = action.attempt,
				attemptItems = state.attemptItems.withSelectedAttempt(action.attempt),
				missingFields = if (action.attempt != null) {
					state.missingFields - EvaluationRequiredField.SUBJECT
				} else {
					state.missingFields
				}
			)
		}

		on<Evaluation.Action.SetType> { state, action ->
			state.copy(
				type = action.type,
				typeItems = state.typeItems.withSelectedType(action.type),
				missingFields = if (action.type != null) {
					state.missingFields - EvaluationRequiredField.TYPE
				} else {
					state.missingFields
				}
			)
		}

		on<Evaluation.Action.SetDate> { state, action ->
			val isOverdue = action.date.isDateInPast()

			state.copy(
				scheduleMode = if (action.date == null) {
					EvaluationScheduleMode.CONTINUOUS
				} else {
					EvaluationScheduleMode.DATED
				},
				date = action.date,
				isOverdue = isOverdue,
				gradeSection = state.gradeSection.updated(
					grade = state.grade,
					maxGrade = state.maxGrade
				)
			)
		}

		on<Evaluation.Action.SetGrade> { state, action ->
			state.copy(
				grade = action.grade,
				gradeSection = state.gradeSection.updated(
					grade = action.grade,
					maxGrade = state.maxGrade
				)
			)
		}

		on<Evaluation.Action.SetMaxGrade> { state, action ->
			val maxGrade = action.maxGrade.takeIf { value -> value > MIN_EVALUATION_GRADE }

			state.copy(
				maxGrade = maxGrade,
				gradeSection = state.gradeSection.updated(
					grade = state.grade,
					maxGrade = maxGrade
				),
				missingFields = if (maxGrade != null) {
					state.missingFields - EvaluationRequiredField.MAX_GRADE
				} else {
					state.missingFields
				}
			)
		}

		// The dialog titles arrive as payload on purpose: they are UI-formatted
		// presentation strings (stringResource with the type label), not values the
		// table can derive from S.
		on<Evaluation.Action.ClickGrade>(
			emits = setOf(Evaluation.Effect.NavigateToGradePickerDialog::class)
		) { state, action ->
			if (action.maxGrade == null || action.maxGrade <= MIN_EVALUATION_GRADE) {
				return@on state
			}

			host.sendEffect(
				Evaluation.Effect.NavigateToGradePickerDialog(
					evaluationName = action.evaluationName,
					subjectCode = action.subjectCode,
					grade = action.grade,
					maxGrade = action.maxGrade
				)
			)
			state
		}

		on<Evaluation.Action.ClickMaxGrade>(
			emits = setOf(Evaluation.Effect.NavigateToMaxGradePickerDialog::class)
		) { state, action ->
			host.sendEffect(
				Evaluation.Effect.NavigateToMaxGradePickerDialog(
					evaluationName = action.evaluationName,
					subjectCode = action.subjectCode,
					maxGrade = action.maxGrade
				)
			)
			state
		}

		// Missing required fields surface inline all at once instead of one
		// snackbar per submit attempt; the use case validator stays as backstop.
		on<Evaluation.Action.ClickSubmitEvaluation> { state, _ ->
			val missingFields = state.missingRequiredFields

			if (missingFields.isEmpty()) {
				machine.submit(host = host, state = state)
				state
			} else {
				state.copy(missingFields = missingFields)
			}
		}

		on<EvaluationInternalEvent.SubmitStarted> { state, _ ->
			state.copy(isSubmitting = true)
		}

		on<EvaluationInternalEvent.SubmitSucceeded>(
			emits = setOf(
				Evaluation.Effect.ShowSnackBar::class,
				Evaluation.Effect.NavigateToEvaluations::class
			)
		) { state, event ->
			host.sendEffect(Evaluation.Effect.ShowSnackBar(message = event.message))
			host.sendEffect(Evaluation.Effect.NavigateToEvaluations)

			state.copy(isSubmitting = false)
		}

		on<EvaluationInternalEvent.SubmitFailed>(
			emits = setOf(
				Evaluation.Effect.ShowSnackBar::class,
				Evaluation.Effect.NavigateToEvaluations::class
			)
		) { state, event ->
			host.sendEffect(Evaluation.Effect.ShowSnackBar(message = event.message))

			if (event.navigateBack) {
				host.sendEffect(Evaluation.Effect.NavigateToEvaluations)
			}

			state.copy(isSubmitting = false)
		}
	}
}
