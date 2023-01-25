package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase

class EvaluationPlanViewModel(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val removeEvaluationUseCase: RemoveEvaluationUseCase
) : ViewModel() {
	val evaluations = LiveResult<List<Evaluation>, Nothing>()
	val updateEvaluation = LiveResult<Evaluation, EvaluationError>()
	val removeEvaluation = LiveCompletable<Nothing>()

	fun getEvaluations(sid: String) =
		execute(useCase = getEvaluationsUseCase, params = sid, liveData = evaluations)

	fun updateEvaluation(params: UpdateEvaluationParams) =
		execute(
			useCase = updateEvaluationUseCase,
			params = params,
			liveData = updateEvaluation
		)

	fun removeEvaluation(id: String) =
		execute(useCase = removeEvaluationUseCase, params = id, liveData = removeEvaluation)
}