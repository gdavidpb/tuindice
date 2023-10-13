package com.gdavidpb.tuindice.evaluations.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.utils.MIN_EVALUATION_GRADE

class UpdateEvaluationParamsValidator : ParamsValidator<UpdateEvaluationParams> {
	override fun validate(params: UpdateEvaluationParams) {
		if (params.grade != null) {
			require(params.grade != 0.0) {
				throw EvaluationIllegalArgumentException(EvaluationError.GradeMissed)
			}

			require(params.grade % MIN_EVALUATION_GRADE == 0.0) {
				throw EvaluationIllegalArgumentException(EvaluationError.InvalidGradeStep)
			}

			require(params.grade in MIN_EVALUATION_GRADE..MAX_EVALUATION_GRADE) {
				throw EvaluationIllegalArgumentException(EvaluationError.OutOfRangeGrade)
			}
		}
	}
}