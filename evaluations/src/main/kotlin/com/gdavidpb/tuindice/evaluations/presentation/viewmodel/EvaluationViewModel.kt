package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AvailableSubjectsReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationReducer

class EvaluationViewModel(
	private val getAvailableSubjectsUseCase: GetAvailableSubjectsUseCase,
	private val getEvaluationUseCase: GetEvaluationUseCase,
	private val availableSubjectsReducer: AvailableSubjectsReducer,
	private val evaluationReducer: EvaluationReducer
) : BaseViewModel<Evaluation.State, Evaluation.Action, Evaluation.Event>(initialViewState = Evaluation.State.Loading) {

	fun loadAvailableSubjectsAction() =
		emitAction(Evaluation.Action.LoadAvailableSubjects)

	fun loadEvaluationAction(params: GetEvaluationParams) =
		emitAction(Evaluation.Action.LoadEvaluation(params))

	override suspend fun reducer(action: Evaluation.Action) {
		when (action) {
			is Evaluation.Action.LoadAvailableSubjects ->
				getAvailableSubjectsUseCase
					.execute(params = Unit)
					.collect(viewModel = this, reducer = availableSubjectsReducer)

			is Evaluation.Action.LoadEvaluation ->
				getEvaluationUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = evaluationReducer)
		}
	}
}