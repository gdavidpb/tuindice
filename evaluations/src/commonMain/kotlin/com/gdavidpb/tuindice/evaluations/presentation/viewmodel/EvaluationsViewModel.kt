package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey

class EvaluationsViewModel(
	override val screenMachine: EvaluationsMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Evaluations.State, Evaluations.Action, Evaluations.Effect>(
	name = "evaluations",
	initialState = screenMachine.initialState(),
	initialAction = Evaluations.Action.LoadEvaluations,
	dispatchers = dispatchers
) {
	fun loadEvaluationsAction() =
		sendAction(Evaluations.Action.LoadEvaluations)

	fun ensureEvaluationsLoadedAction() =
		sendAction(Evaluations.Action.EnsureEvaluationsLoaded)

	fun refreshEvaluationsAction() =
		sendAction(Evaluations.Action.RefreshEvaluations)

	fun selectWeekAction(weekKey: EvaluationsWeekKey) =
		sendAction(Evaluations.Action.SelectWeek(weekKey))

	fun addEvaluationAction() =
		sendAction(Evaluations.Action.AddEvaluation)

	fun editEvaluationAction(evaluationId: String) =
		sendAction(Evaluations.Action.EditEvaluation(evaluationId))

	fun removeEvaluationAction(evaluationId: String) =
		sendAction(Evaluations.Action.RemoveEvaluation(evaluationId))

	fun showEvaluationGradeDialogAction(evaluationId: String, evaluationName: String, subjectCode: String) =
		sendAction(Evaluations.Action.ShowEvaluationGradeDialog(evaluationId, evaluationName, subjectCode))

	fun setEvaluationGradeAction(evaluationId: String, grade: Double) =
		sendAction(Evaluations.Action.SetEvaluationGrade(evaluationId, grade))
}
