package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationsReducer

class EvaluationsViewModel(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val evaluationsReducer: EvaluationsReducer,
	private val resourceResolver: ResourceResolver
) : BaseViewModel<Evaluations.State, Evaluations.Action, Evaluations.Event>(initialViewState = Evaluations.State.Loading) {

	fun loadEvaluationsAction() =
		emitAction(Evaluations.Action.LoadEvaluations)

	fun filterEvaluationsAction(filters: List<EvaluationFilter>) =
		emitAction(Evaluations.Action.FilterEvaluations(filters))

	fun addEvaluationAction() =
		emitAction(Evaluations.Action.AddEvaluation)

	fun editEvaluationAction(evaluationId: String) =
		emitAction(Evaluations.Action.EditEvaluation(evaluationId))

	fun clearFiltersAction() =
		emitAction(Evaluations.Action.FilterEvaluations(emptyList()))

	override suspend fun reducer(action: Evaluations.Action) {
		when (action) {
			is Evaluations.Action.LoadEvaluations -> {
				val currentState = getCurrentState()

				val filters =
					if (currentState is Evaluations.State.Content && currentState.activeFilters.isNotEmpty())
						currentState.activeFilters
					else
						listOf()

				getEvaluationsUseCase
					.execute(params = filters)
					.collect(viewModel = this, reducer = evaluationsReducer)
			}

			is Evaluations.Action.FilterEvaluations ->
				getEvaluationsUseCase
					.execute(params = action.filters)
					.collect(viewModel = this, reducer = evaluationsReducer)

			is Evaluations.Action.AddEvaluation ->
				sendEvent(
					Evaluations.Event.NavigateToAddEvaluation(
						title = resourceResolver.getString(R.string.title_add_evaluation)
					)
				)

			is Evaluations.Action.EditEvaluation ->
				sendEvent(
					Evaluations.Event.NavigateToEvaluation(
						title = resourceResolver.getString(R.string.title_update_evaluation),
						evaluationId = action.evaluationId
					)
				)
		}
	}
}