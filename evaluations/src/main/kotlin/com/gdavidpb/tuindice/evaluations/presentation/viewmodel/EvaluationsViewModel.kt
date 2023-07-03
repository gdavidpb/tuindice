package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationsReducer

class EvaluationsViewModel(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val evaluationsReducer: EvaluationsReducer
) : BaseViewModel<Evaluations.State, Evaluations.Action, Evaluations.Event>(initialViewState = Evaluations.State.Loading) {

	fun loadEvaluationsAction(filters: List<EvaluationFilter> = listOf()) =
		emitAction(Evaluations.Action.LoadEvaluation(filters))

	fun addEvaluationAction() =
		emitAction(Evaluations.Action.AddEvaluation)

	fun openEvaluationsFilters() =
		emitAction(Evaluations.Action.OpenEvaluationsFilters)

	fun closeDialogAction() =
		emitAction(Evaluations.Action.CloseDialog)

	override suspend fun reducer(action: Evaluations.Action) {
		when (action) {
			is Evaluations.Action.LoadEvaluation ->
				getEvaluationsUseCase
					.execute(params = action.filters)
					.collect(viewModel = this, reducer = evaluationsReducer)

			is Evaluations.Action.AddEvaluation ->
				TODO()

			is Evaluations.Action.OpenEvaluationsFilters -> {
				when (val currentState = getCurrentState()) {
					is Evaluations.State.Content -> {
						sendEvent(
							Evaluations.Event.ShowFilterEvaluationsDialog(
								availableFilters = currentState.availableFilters,
								activeFilters = currentState.activeFilters
							)
						)
					}

					is Evaluations.State.EmptyMatch ->
						sendEvent(
							Evaluations.Event.ShowFilterEvaluationsDialog(
								availableFilters = currentState.availableFilters,
								activeFilters = currentState.activeFilters
							)
						)

					else -> {}
				}
			}

			is Evaluations.Action.CloseDialog ->
				sendEvent(Evaluations.Event.CloseDialog)
		}
	}
}