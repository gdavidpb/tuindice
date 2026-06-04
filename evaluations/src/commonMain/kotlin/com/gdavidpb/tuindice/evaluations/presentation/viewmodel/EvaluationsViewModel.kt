package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RefreshEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SelectEvaluationsWeekActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow

class EvaluationsViewModel(
	private val loadEvaluationsActionProcessor: LoadEvaluationsActionProcessor,
	private val refreshEvaluationsActionProcessor: RefreshEvaluationsActionProcessor,
	private val selectEvaluationsWeekActionProcessor: SelectEvaluationsWeekActionProcessor,
	private val openAddEvaluationActionProcessor: OpenAddEvaluationActionProcessor,
	private val pickEvaluationGradeActionProcessor: PickEvaluationGradeActionProcessor,
	private val setEvaluationGradeActionProcessor: SetEvaluationGradeActionProcessor,
	private val openEvaluationActionProcessor: OpenEvaluationActionProcessor,
	private val removeEvaluationActionProcessor: RemoveEvaluationActionProcessor,
	override val eventPublisher: EventPublisher
) : BaseViewModel<Evaluations.State, Evaluations.Action, Evaluations.Effect>(
	name = "evaluations",
	initialState = Evaluations.State.Idle
) {
	fun loadEvaluationsAction() =
		sendAction(Evaluations.Action.LoadEvaluations)

	fun refreshEvaluationsAction() =
		sendAction(Evaluations.Action.RefreshEvaluations)

	fun selectWeekAction(weekNumber: Int) =
		sendAction(Evaluations.Action.SelectWeek(weekNumber))

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

	override suspend fun processAction(
		action: Evaluations.Action,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return when (action) {
			is Evaluations.Action.LoadEvaluations ->
				loadEvaluationsActionProcessor.process(action, sideEffect)

			is Evaluations.Action.RefreshEvaluations ->
				refreshEvaluationsActionProcessor.process(action, sideEffect)

			is Evaluations.Action.SelectWeek ->
				selectEvaluationsWeekActionProcessor.process(action, sideEffect)

			is Evaluations.Action.AddEvaluation ->
				openAddEvaluationActionProcessor.process(action, sideEffect)

			is Evaluations.Action.ShowEvaluationGradeDialog ->
				pickEvaluationGradeActionProcessor.process(action, sideEffect)

			is Evaluations.Action.SetEvaluationGrade ->
				setEvaluationGradeActionProcessor.process(action, sideEffect)

			is Evaluations.Action.EditEvaluation ->
				openEvaluationActionProcessor.process(action, sideEffect)

			is Evaluations.Action.RemoveEvaluation ->
				removeEvaluationActionProcessor.process(action, sideEffect)
		}
	}
}
