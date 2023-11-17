package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.action.list.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.ClearEvaluationFiltersActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.CloseListDialogActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EvaluationsViewModel(
	private val loadEvaluationsActionProcessor: LoadEvaluationsActionProcessor,
	private val checkEvaluationFilterActionProcessor: CheckEvaluationFilterActionProcessor,
	private val uncheckEvaluationFilterActionProcessor: UncheckEvaluationFilterActionProcessor,
	private val clearEvaluationFiltersActionProcessor: ClearEvaluationFiltersActionProcessor,
	private val openAddEvaluationActionProcessor: OpenAddEvaluationActionProcessor,
	private val pickEvaluationGradeActionProcessor: PickEvaluationGradeActionProcessor,
	private val setEvaluationGradeActionProcessor: SetEvaluationGradeActionProcessor,
	private val openEvaluationActionProcessor: OpenEvaluationActionProcessor,
	private val removeEvaluationActionProcessor: RemoveEvaluationActionProcessor,
	private val closeListDialogActionProcessor: CloseListDialogActionProcessor
) : BaseViewModel<Evaluations.State, Evaluations.Action, Evaluations.Effect>(initialState = Evaluations.State.Loading) {

	private val activeFilters = state
		.filterIsInstance<Evaluations.State.Content>()
		.map { content -> content.activeFilters }
		.distinctUntilChanged()
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = emptyList()
		)

	fun loadEvaluationsAction() =
		sendAction(Evaluations.Action.LoadEvaluations(activeFilters))

	fun toggleFilterAction(filter: EvaluationFilter, isChecked: Boolean) =
		if (isChecked)
			sendAction(Evaluations.Action.CheckEvaluationFilter(filter))
		else
			sendAction(Evaluations.Action.UncheckEvaluationFilter(filter))

	fun clearFiltersAction() =
		sendAction(Evaluations.Action.ClearEvaluationFilters)

	fun addEvaluationAction() =
		sendAction(Evaluations.Action.AddEvaluation)

	fun editEvaluationAction(evaluationId: String) =
		sendAction(Evaluations.Action.EditEvaluation(evaluationId))

	fun removeEvaluationAction(evaluationId: String) =
		sendAction(Evaluations.Action.RemoveEvaluation(evaluationId))

	fun showEvaluationGradeDialogAction(evaluationId: String) =
		sendAction(Evaluations.Action.ShowEvaluationGradeDialog(evaluationId))

	fun setEvaluationGradeAction(evaluationId: String, grade: Double) =
		sendAction(Evaluations.Action.SetEvaluationGrade(evaluationId, grade))

	fun closeDialogAction() =
		sendAction(Evaluations.Action.CloseDialog)

	override fun processAction(
		action: Evaluations.Action,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return when (action) {
			is Evaluations.Action.LoadEvaluations ->
				loadEvaluationsActionProcessor.process(action, sideEffect)

			is Evaluations.Action.CheckEvaluationFilter ->
				checkEvaluationFilterActionProcessor.process(action, sideEffect)

			is Evaluations.Action.UncheckEvaluationFilter ->
				uncheckEvaluationFilterActionProcessor.process(action, sideEffect)

			is Evaluations.Action.ClearEvaluationFilters ->
				clearEvaluationFiltersActionProcessor.process(action, sideEffect)

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

			is Evaluations.Action.CloseDialog ->
				closeListDialogActionProcessor.process(action, sideEffect)
		}
	}
}