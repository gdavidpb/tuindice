package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
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
	val getEvaluationParams = emptyStateFlow<String>()
	val addEvaluationParams = emptyStateFlow<AddEvaluationParams>()
	val updateEvaluationParams = emptyStateFlow<UpdateEvaluationParams>()

	val getEvaluation =
		stateInFlow(useCase = getEvaluationUseCase, paramsFlow = getEvaluationParams)

	val addEvaluation =
		stateInAction(useCase = addEvaluationUseCase, paramsFlow = addEvaluationParams)

	val updateEvaluation =
		stateInAction(useCase = updateEvaluationUseCase, paramsFlow = updateEvaluationParams)
}