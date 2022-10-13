package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuarterUseCase
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.evaluations.domain.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetSubjectEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase

class EvaluationPlanViewModel(
	private val getSubjectEvaluationsUseCase: GetSubjectEvaluationsUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val updateQuarterUseCase: UpdateQuarterUseCase,
	private val removeEvaluationUseCase: RemoveEvaluationUseCase
) : ViewModel() {
	val evaluations = LiveResult<List<Evaluation>, Nothing>()
	val evaluationUpdate = LiveResult<Evaluation, Nothing>()
	val quarterUpdate = LiveResult<Quarter, Nothing>()
	val evaluationRemove = LiveCompletable<Nothing>()

	fun updateQuarter(request: UpdateQuarterRequest) =
		execute(useCase = updateQuarterUseCase, params = request, liveData = quarterUpdate)

	fun getSubjectEvaluations(sid: String) =
		execute(useCase = getSubjectEvaluationsUseCase, params = sid, liveData = evaluations)

	fun updateEvaluation(request: UpdateEvaluationRequest) =
		execute(useCase = updateEvaluationUseCase, params = request, liveData = evaluationUpdate)

	fun removeEvaluation(id: String) =
		execute(useCase = removeEvaluationUseCase, params = id, liveData = evaluationRemove)
}