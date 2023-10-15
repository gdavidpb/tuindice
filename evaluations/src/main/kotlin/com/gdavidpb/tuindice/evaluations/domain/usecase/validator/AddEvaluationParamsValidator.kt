package com.gdavidpb.tuindice.evaluations.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams

class AddEvaluationParamsValidator : ParamsValidator<AddEvaluationParams> {
	override fun validate(params: AddEvaluationParams) {
		require(!params.subjectId.isNullOrBlank()) {
			throw AddEvaluationIllegalArgumentException(AddEvaluationError.SubjectMissed)
		}

		require(params.maxGrade != null) {
			throw AddEvaluationIllegalArgumentException(AddEvaluationError.MaxGradeMissed)
		}

		require(params.type != null) {
			throw AddEvaluationIllegalArgumentException(AddEvaluationError.TypeMissed)
		}
	}
}