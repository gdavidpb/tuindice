package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase

class EvaluationViewModel(
	private val getEvaluationUseCase: GetEvaluationUseCase,
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase
) : ViewModel() {
	val evaluation = LiveResult<Evaluation, Nothing>()
	val add = LiveResult<Evaluation, Nothing>()
	val evaluationUpdate = LiveResult<Evaluation, Nothing>()

	fun getEvaluation(eid: String) =
		execute(useCase = getEvaluationUseCase, params = eid, liveData = evaluation)

	fun addEvaluation(evaluation: Evaluation) =
		execute(useCase = addEvaluationUseCase, params = evaluation, liveData = add)

	fun updateEvaluation(request: UpdateEvaluationParams) =
		execute(useCase = updateEvaluationUseCase, params = request, liveData = evaluationUpdate)
}