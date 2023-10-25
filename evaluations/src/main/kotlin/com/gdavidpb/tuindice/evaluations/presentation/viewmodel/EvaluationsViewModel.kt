package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationsReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.RemoveEvaluationReducer

class EvaluationsViewModel(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val evaluationsReducer: EvaluationsReducer,
	private val removeEvaluationUseCase: RemoveEvaluationUseCase,
	private val removeEvaluationReducer: RemoveEvaluationReducer,
	private val resourceResolver: ResourceResolver
) : BaseViewModel<Evaluations.State, Evaluations.Action, Evaluations.Event>(initialViewState = Evaluations.State.Loading) {

	fun loadEvaluationsAction() =
		emitAction(Evaluations.Action.LoadEvaluations)

	fun filterEvaluationsAction(filter: EvaluationFilter, isChecked: Boolean) =
		if (isChecked)
			emitAction(Evaluations.Action.CheckEvaluationFilter(filter))
		else
			emitAction(Evaluations.Action.UncheckEvaluationFilter(filter))

	fun addEvaluationAction() =
		emitAction(Evaluations.Action.AddEvaluation)

	fun editEvaluationAction(evaluationId: String) =
		emitAction(Evaluations.Action.EditEvaluation(evaluationId))

	fun removeEvaluationAction(evaluationId: String) =
		emitAction(Evaluations.Action.RemoveEvaluation(evaluationId))

	fun confirmRemoveEvaluationAction(evaluationId: String) =
		emitAction(Evaluations.Action.ConfirmRemoveEvaluation(evaluationId))

	fun updateEvaluationIsCompletedAction(evaluationId: String, isCompleted: Boolean) =
		if (isCompleted)
			emitAction(Evaluations.Action.CheckEvaluationAsCompleted(evaluationId))
		else
			emitAction(Evaluations.Action.UncheckEvaluationAsCompleted(evaluationId))

	fun closeDialogAction() =
		emitAction(Evaluations.Action.CloseDialog)

	override suspend fun reducer(action: Evaluations.Action) {
		when (action) {
			is Evaluations.Action.LoadEvaluations ->
				getEvaluationsUseCase
					.execute(params = emptyList())
					.collect(viewModel = this, reducer = evaluationsReducer)

			is Evaluations.Action.CheckEvaluationFilter -> {
				val currentState = getCurrentState()

				if (currentState is Evaluations.State.Content) {
					val activeFilters = currentState.activeFilters + action.filter

					getEvaluationsUseCase
						.execute(params = activeFilters)
						.collect(viewModel = this, reducer = evaluationsReducer)
				}
			}

			is Evaluations.Action.UncheckEvaluationFilter -> {
				val currentState = getCurrentState()

				if (currentState is Evaluations.State.Content) {
					val activeFilters = currentState.activeFilters - action.filter

					getEvaluationsUseCase
						.execute(params = activeFilters)
						.collect(viewModel = this, reducer = evaluationsReducer)
				}
			}

			is Evaluations.Action.AddEvaluation ->
				sendEvent(
					Evaluations.Event.NavigateToAddEvaluation
				)

			is Evaluations.Action. RemoveEvaluation ->
				sendEvent(
					Evaluations.Event.ShowRemoveEvaluationDialog(
						evaluationId = action.evaluationId,
						message = resourceResolver.getString(
							R.string.dialog_message_remove_evaluation
						)
					)
				)

			is Evaluations.Action.ConfirmRemoveEvaluation ->
				removeEvaluationUseCase
					.execute(params = action.evaluationId)
					.collect(viewModel = this, reducer = removeEvaluationReducer)

			is Evaluations.Action.CheckEvaluationAsCompleted -> {
				// TODO CheckEvaluationAsCompleted
			}

			is Evaluations.Action.UncheckEvaluationAsCompleted -> {
				// TODO UncheckEvaluationAsCompleted
			}

			is Evaluations.Action.EditEvaluation ->
				sendEvent(
					Evaluations.Event.NavigateToEvaluation(
						evaluationId = action.evaluationId
					)
				)

			is Evaluations.Action.CloseDialog ->
				sendEvent(
					Evaluations.Event.CloseDialog
				)
		}
	}
}