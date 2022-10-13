package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.evaluations.domain.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.utils.mappers.applyUpdate

class UpdateEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val databaseRepository: DatabaseRepository
) : ResultUseCase<UpdateEvaluationRequest, Evaluation, Nothing>() {
	override suspend fun executeOnBackground(params: UpdateEvaluationRequest): Evaluation {
		val activeUId = authRepository.getActiveAuth().uid

		return if (params.dispatchChanges)
			databaseRepository
				.updateEvaluation(uid = activeUId, eid = params.eid, update = params.update)
		else
			databaseRepository
				.getEvaluation(uid = activeUId, eid = params.eid)
				.applyUpdate(update = params.update)
	}
}