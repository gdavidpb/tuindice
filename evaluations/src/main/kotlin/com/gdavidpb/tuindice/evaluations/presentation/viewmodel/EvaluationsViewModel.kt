package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetFiltersUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationsReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.FiltersReducer

class EvaluationsViewModel(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val getFiltersUseCase: GetFiltersUseCase,
	private val evaluationsReducer: EvaluationsReducer,
	private val filtersReducer: FiltersReducer
) : BaseViewModel<Evaluations.State, Evaluations.Action, Evaluations.Event>(initialViewState = Evaluations.State.Loading) {

	fun loadEvaluationsAction() =
		emitAction(Evaluations.Action.LoadEvaluation)

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
					.execute(params = Unit)
					.collect(viewModel = this, reducer = evaluationsReducer)

			is Evaluations.Action.AddEvaluation ->
				TODO()

			is Evaluations.Action.OpenEvaluationsFilters -> {
				val currentState = getCurrentState()

				if (currentState is Evaluations.State.Content) {
					getFiltersUseCase
						.execute(params = currentState.evaluations)
						.collect(viewModel = this, reducer = filtersReducer)
				}
			}

			is Evaluations.Action.CloseDialog ->
				sendEvent(Evaluations.Event.CloseDialog)
		}
	}
}