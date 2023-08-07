package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toUpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AddEvaluationReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.EvaluationAndAvailableSubjectsReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.UpdateEvaluationReducer

class EvaluationViewModel(
	private val getEvaluationAndAvailableSubjectsUseCase: GetEvaluationAndAvailableSubjectsUseCase,
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val evaluationAndAvailableSubjectsReducer: EvaluationAndAvailableSubjectsReducer,
	private val addEvaluationReducer: AddEvaluationReducer,
	private val updateEvaluationReducer: UpdateEvaluationReducer
) : BaseViewModel<Evaluation.State, Evaluation.Action, Evaluation.Event>(initialViewState = Evaluation.State.Loading) {

	fun loadEvaluationAction(evaluationId: String) =
		emitAction(Evaluation.Action.LoadEvaluation(evaluationId))

	fun saveEvaluationAction() {
		val currentState = getCurrentState()

		if (currentState is Evaluation.State.Content)
			emitAction(
				Evaluation.Action.ClickSaveEvaluation(
					evaluationId = currentState.evaluationId,
					subject = currentState.subject,
					name = currentState.name,
					date = currentState.date,
					grade = currentState.grade,
					maxGrade = currentState.maxGrade,
					type = currentState.type,
					exists = currentState.exists,
					isCompleted = currentState.isCompleted
				)
			)
	}

	override suspend fun reducer(action: Evaluation.Action) {
		when (action) {
			is Evaluation.Action.LoadEvaluation ->
				getEvaluationAndAvailableSubjectsUseCase
					.execute(
						params = GetEvaluationParams(
							evaluationId = action.evaluationId
						)
					)
					.collect(viewModel = this, reducer = evaluationAndAvailableSubjectsReducer)

			is Evaluation.Action.ClickSaveEvaluation ->
				if (action.exists)
					updateEvaluationUseCase
						.execute(params = action.toUpdateEvaluationParams())
						.collect(viewModel = this, reducer = updateEvaluationReducer)
				else
					addEvaluationUseCase
						.execute(params = action.toAddEvaluationParams())
						.collect(viewModel = this, reducer = addEvaluationReducer)
		}
	}
}