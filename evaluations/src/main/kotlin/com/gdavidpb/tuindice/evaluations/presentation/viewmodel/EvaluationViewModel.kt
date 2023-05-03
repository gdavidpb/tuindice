package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AddEvaluationReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.UpdateEvaluationReducer

class EvaluationViewModel(
	private val getEvaluationUseCase: GetEvaluationUseCase,
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val evaluationReducer: EvaluationReducer,
	private val addEvaluationReducer: AddEvaluationReducer,
	private val updateEvaluationReducer: UpdateEvaluationReducer
) : BaseViewModel<Evaluations.State, Evaluations.Action, Evaluations.Event>(initialViewState = Evaluations.State.Loading) {

	fun loadEvaluationAction(params: GetEvaluationParams) =
		emitAction(Evaluations.Action.LoadEvaluation(params))

	fun addEvaluationAction(params: AddEvaluationParams) =
		emitAction(Evaluations.Action.AddEvaluation(params))

	fun updateEvaluationAction(params: UpdateEvaluationParams) =
		emitAction(Evaluations.Action.UpdateEvaluation(params))

	override suspend fun reducer(action: Evaluations.Action) {
		when (action) {
			is Evaluations.Action.LoadEvaluation ->
				getEvaluationUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = evaluationReducer)

			is Evaluations.Action.AddEvaluation ->
				addEvaluationUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = addEvaluationReducer)

			is Evaluations.Action.UpdateEvaluation ->
				updateEvaluationUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = updateEvaluationReducer)
		}
	}
}