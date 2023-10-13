package com.gdavidpb.tuindice.evaluations.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.utils.MIN_EVALUATION_GRADE
import kotlin.math.min

class AddEvaluationParamsValidator : ParamsValidator<AddEvaluationParams> {
	override fun validate(params: AddEvaluationParams) {
		require(!params.subjectId.isNullOrBlank()) {
			throw EvaluationIllegalArgumentException(EvaluationError.SubjectMissed)
		}

		require(params.grade != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.GradeMissed)
		}

		require(params.maxGrade != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.MaxGradeMissed)
		}

		require(params.grade % MIN_EVALUATION_GRADE == 0.0) {
			throw EvaluationIllegalArgumentException(EvaluationError.InvalidGradeStep)
		}

		require(params.grade in MIN_EVALUATION_GRADE..min(params.maxGrade, MAX_EVALUATION_GRADE)) {
			throw EvaluationIllegalArgumentException(EvaluationError.OutOfRangeGrade)
		}

		require(params.type != null) {
			throw EvaluationIllegalArgumentException(EvaluationError.TypeMissed)
		}
	}
}