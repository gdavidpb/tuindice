package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.emit
import com.gdavidpb.tuindice.base.utils.extension.emptyStateFlow
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.base.utils.extension.stateInFlow
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase

class EvaluationViewModel(
	getEvaluationUseCase: GetEvaluationUseCase,
	addEvaluationUseCase: AddEvaluationUseCase,
	updateEvaluationUseCase: UpdateEvaluationUseCase
) : ViewModel() {
	private val getEvaluationParams = emptyStateFlow<String>()
	private val addEvaluationParams = emptyStateFlow<AddEvaluationParams>()
	private val updateEvaluationParams = emptyStateFlow<UpdateEvaluationParams>()

	fun getEvaluation(evaluationId: String) =
		emit(getEvaluationParams, evaluationId)

	fun addEvaluation(params: AddEvaluationParams) =
		emit(addEvaluationParams, params)

	fun updateEvaluation(params: UpdateEvaluationParams) =
		emit(updateEvaluationParams, params)

	val getEvaluation =
		stateInFlow(useCase = getEvaluationUseCase, paramsFlow = getEvaluationParams)

	val addEvaluation =
		stateInAction(useCase = addEvaluationUseCase, paramsFlow = addEvaluationParams)

	val updateEvaluation =
		stateInAction(useCase = updateEvaluationUseCase, paramsFlow = updateEvaluationParams)
}