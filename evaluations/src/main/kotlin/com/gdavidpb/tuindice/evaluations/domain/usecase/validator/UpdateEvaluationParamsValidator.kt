package com.gdavidpb.tuindice.evaluations.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.exception.UpdateEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams

class UpdateEvaluationParamsValidator : ParamsValidator<UpdateEvaluationParams> {
	override fun validate(params: UpdateEvaluationParams) {
		require(!params.subjectId.isNullOrBlank()) {
			throw UpdateEvaluationIllegalArgumentException(UpdateEvaluationError.SubjectMissed)
		}

		require(params.maxGrade != null) {
			throw UpdateEvaluationIllegalArgumentException(UpdateEvaluationError.MaxGradeMissed)
		}

		require(params.type != null) {
			throw UpdateEvaluationIllegalArgumentException(UpdateEvaluationError.TypeMissed)
		}
	}
}