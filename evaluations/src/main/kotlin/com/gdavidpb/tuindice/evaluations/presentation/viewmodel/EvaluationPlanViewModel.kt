package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.base.utils.extension.stateInFlow
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

class EvaluationPlanViewModel(
	getEvaluationsUseCase: GetEvaluationsUseCase,
	updateEvaluationUseCase: UpdateEvaluationUseCase,
	removeEvaluationUseCase: RemoveEvaluationUseCase
) : ViewModel() {
	val getEvaluationsParams = MutableSharedFlow<String>()
	val updateEvaluationParams = MutableSharedFlow<UpdateEvaluationParams>()
	val removeEvaluationParams = MutableSharedFlow<String>()

	val getEvaluations =
		stateInFlow(useCase = getEvaluationsUseCase, paramsFlow = getEvaluationsParams)

	val updateEvaluation =
		stateInAction(useCase = updateEvaluationUseCase, paramsFlow = updateEvaluationParams)

	val removeEvaluation =
		stateInAction(useCase = removeEvaluationUseCase, paramsFlow = removeEvaluationParams)
}