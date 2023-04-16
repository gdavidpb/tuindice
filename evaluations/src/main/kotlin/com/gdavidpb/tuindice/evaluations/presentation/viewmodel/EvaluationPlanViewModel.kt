package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.emit
import com.gdavidpb.tuindice.base.utils.extension.emptyStateFlow
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.base.utils.extension.stateInFlow
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase

class EvaluationPlanViewModel(
	getEvaluationsUseCase: GetEvaluationsUseCase,
	updateEvaluationUseCase: UpdateEvaluationUseCase,
	removeEvaluationUseCase: RemoveEvaluationUseCase
) : ViewModel() {
	private val getEvaluationsParams = emptyStateFlow<String>()
	private val updateEvaluationParams = emptyStateFlow<UpdateEvaluationParams>()
	private val removeEvaluationParams = emptyStateFlow<String>()

	fun getEvaluations(subjectId: String) =
		emit(getEvaluationsParams, subjectId)

	fun updateEvaluation(params: UpdateEvaluationParams) =
		emit(updateEvaluationParams, params)

	fun removeEvaluation(evaluationId: String) =
		emit(removeEvaluationParams, evaluationId)

	val getEvaluations =
		stateInFlow(useCase = getEvaluationsUseCase, paramsFlow = getEvaluationsParams)

	val updateEvaluation =
		stateInAction(useCase = updateEvaluationUseCase, paramsFlow = updateEvaluationParams)

	val removeEvaluation =
		stateInAction(useCase = removeEvaluationUseCase, paramsFlow = removeEvaluationParams)
}