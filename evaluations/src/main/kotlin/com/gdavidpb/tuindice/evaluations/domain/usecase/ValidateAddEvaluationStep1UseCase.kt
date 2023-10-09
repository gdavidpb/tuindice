package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.ValidateAddEvaluationStep1Params
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ValidateAddEvaluationStep1UseCase
	: FlowUseCase<ValidateAddEvaluationStep1Params, Unit, EvaluationError>() {
	override suspend fun executeOnBackground(params: ValidateAddEvaluationStep1Params): Flow<Unit> {
		require(params.subject != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.SubjectMissed)
		}

		require(params.type != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.TypeMissed)
		}

		return flowOf(Unit)
	}
}