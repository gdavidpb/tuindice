package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams

class UpdateEvaluationUseCase(
) : ResultUseCase<UpdateEvaluationParams, Evaluation, Nothing>() {
	override suspend fun executeOnBackground(params: UpdateEvaluationParams): Evaluation {
		TODO("Not yet implemented")
		/*
		val activeUId = authRepository.getActiveAuth().uid

		return if (params.dispatchChanges)
			databaseRepository
				.updateEvaluation(uid = activeUId, eid = params.eid, update = params.update)
		else
			databaseRepository
				.getEvaluation(uid = activeUId, eid = params.eid)
				.applyUpdate(update = params.update)
		 */
	}
}