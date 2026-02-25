package com.gdavidpb.tuindice.evaluations.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams

class AddEvaluationParamsValidator : ParamsValidator<AddEvaluationParams> {
	override fun validate(params: AddEvaluationParams) {
		require(!params.subjectId.isNullOrBlank()) {
			throw AddEvaluationIllegalArgumentException(AddEvaluationUseCaseError.SubjectMissed)
		}

		require(params.maxGrade != null) {
			throw AddEvaluationIllegalArgumentException(AddEvaluationUseCaseError.MaxGradeMissed)
		}

		require(params.type != null) {
			throw AddEvaluationIllegalArgumentException(AddEvaluationUseCaseError.TypeMissed)
		}
	}
}