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

	fun filterEvaluationsAction(filter: EvaluationFilter, isChecked: Boolean) =
		if (isChecked)
			emitAction(Evaluations.Action.CheckEvaluationFilter(filter))
		else
			emitAction(Evaluations.Action.UncheckEvaluationFilter(filter))

	fun addEvaluationAction() =
		emitAction(Evaluations.Action.AddEvaluation)

	fun editEvaluationAction(evaluationId: String) =
		emitAction(Evaluations.Action.EditEvaluation(evaluationId))

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