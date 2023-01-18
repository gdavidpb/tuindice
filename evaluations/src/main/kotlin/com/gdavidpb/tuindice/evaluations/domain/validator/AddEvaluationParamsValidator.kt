package com.gdavidpb.tuindice.evaluations.domain.validator

import com.gdavidpb.tuindice.base.domain.validator.Validator
import com.gdavidpb.tuindice.base.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.base.utils.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams

class AddEvaluationParamsValidator : Validator<AddEvaluationParams> {
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