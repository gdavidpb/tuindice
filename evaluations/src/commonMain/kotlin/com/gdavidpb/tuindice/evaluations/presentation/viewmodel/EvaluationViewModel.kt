package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationMachine

class EvaluationViewModel(
	override val screenMachine: EvaluationMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Evaluation.State, Evaluation.Action, Evaluation.Effect>(
	name = "evaluation",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun setAttemptAction(attempt: EditableAttemptDescriptor?) =
		sendAction(Evaluation.Action.SetAttempt(attempt))

	fun setTypeAction(type: EvaluationType?) =
		sendAction(Evaluation.Action.SetType(type))

	fun setDateAction(date: Long?) =
		sendAction(Evaluation.Action.SetDate(date))

	fun setGradeAction(grade: Double) =
		sendAction(Evaluation.Action.SetGrade(grade))

	fun setMaxGradeAction(grade: Double) =
		sendAction(Evaluation.Action.SetMaxGrade(grade))

	fun loadAvailableAttemptsAction() =
		sendAction(Evaluation.Action.LoadAvailableAttempts)

	fun loadEvaluationAction(evaluationId: String) =
		sendAction(Evaluation.Action.LoadEvaluation(evaluationId))

	fun submitEvaluationAction() =
		sendAction(Evaluation.Action.ClickSubmitEvaluation)

	fun clickGradeAction(evaluationName: String, subjectCode: String, grade: Double?, maxGrade: Double?) =
		sendAction(Evaluation.Action.ClickGrade(evaluationName, subjectCode, grade, maxGrade))

	fun clickMaxGradeAction(evaluationName: String, subjectCode: String, maxGrade: Double?) =
		sendAction(Evaluation.Action.ClickMaxGrade(evaluationName, subjectCode, maxGrade))
}
