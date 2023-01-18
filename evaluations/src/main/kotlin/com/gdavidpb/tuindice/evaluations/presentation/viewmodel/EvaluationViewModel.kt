package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError

class EvaluationViewModel(
	private val getEvaluationUseCase: GetEvaluationUseCase,
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase
) : ViewModel() {
	val getEvaluation = LiveResult<Evaluation, Nothing>()
	val addOrUpdateEvaluation = LiveResult<Evaluation, EvaluationError>()

	fun getEvaluation(evaluationId: String) =
		execute(useCase = getEvaluationUseCase, params = evaluationId, liveData = getEvaluation)

	fun addEvaluation(addEvaluationParams: AddEvaluationParams) =
		execute(
			useCase = addEvaluationUseCase,
			params = addEvaluationParams,
			liveData = addOrUpdateEvaluation
		)

	fun updateEvaluation(updateEvaluationParams: UpdateEvaluationParams) =
		execute(
			useCase = updateEvaluationUseCase,
			params = updateEvaluationParams,
			liveData = addOrUpdateEvaluation
		)
}