package com.gdavidpb.tuindice.evaluations.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.utils.MIN_EVALUATION_GRADE

class AddEvaluationParamsValidator : ParamsValidator<AddEvaluationParams> {
	override fun validate(params: AddEvaluationParams) {
		require(!params.name.isNullOrBlank()) {
			throw EvaluationIllegalArgumentException(EvaluationError.EmptyName)
		}

		require(params.grade != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.GradeMissed)
		}

		require(params.grade % MIN_EVALUATION_GRADE == 0.0) {
			throw EvaluationIllegalArgumentException(EvaluationError.InvalidGradeStep)
		}

		require(params.grade in MIN_EVALUATION_GRADE..MAX_EVALUATION_GRADE) {
			throw EvaluationIllegalArgumentException(EvaluationError.OutOfRangeGrade)
		}

		require(params.date != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.DateMissed)
		}

		require(params.type != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.TypeMissed)
		}
	}
}