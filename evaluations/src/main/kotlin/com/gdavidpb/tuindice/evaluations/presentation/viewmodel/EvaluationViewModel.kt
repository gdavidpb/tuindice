package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationAndAvailableSubjectsReducer

class EvaluationViewModel(
	private val getEvaluationAndAvailableSubjectsUseCase: GetEvaluationAndAvailableSubjectsUseCase,
	private val evaluationAndAvailableSubjectsReducer: EvaluationAndAvailableSubjectsReducer
) : BaseViewModel<Evaluation.State, Evaluation.Action, Evaluation.Event>(initialViewState = Evaluation.State.Loading) {

	fun loadEvaluationAction(params: GetEvaluationParams) =
		emitAction(Evaluation.Action.LoadEvaluation(params))

	override suspend fun reducer(action: Evaluation.Action) {
		when (action) {
			is Evaluation.Action.LoadEvaluation ->
				getEvaluationAndAvailableSubjectsUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = evaluationAndAvailableSubjectsReducer)
		}
	}
}