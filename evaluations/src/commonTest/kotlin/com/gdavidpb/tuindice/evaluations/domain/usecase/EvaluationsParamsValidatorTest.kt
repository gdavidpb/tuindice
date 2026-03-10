package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EvaluationsParamsValidatorTest {
	@Test
	fun addEvaluationParamsValidator_acceptsRequiredFields() {
		AddEvaluationParamsValidator().validate(validParams())
	}

	@Test
	fun addEvaluationParamsValidator_rejectsMissingSubjectId() {
		val exception = assertFailsWith<AddEvaluationIllegalArgumentException> {
			AddEvaluationParamsValidator().validate(validParams(subjectId = null))
		}

		assertEquals(AddEvaluationUseCaseError.SubjectMissed, exception.error)
	}

	@Test
	fun addEvaluationParamsValidator_rejectsMissingMaxGrade() {
		val exception = assertFailsWith<AddEvaluationIllegalArgumentException> {
			AddEvaluationParamsValidator().validate(validParams(maxGrade = null))
		}

		assertEquals(AddEvaluationUseCaseError.MaxGradeMissed, exception.error)
	}

	@Test
	fun addEvaluationParamsValidator_rejectsMissingType() {
		val exception = assertFailsWith<AddEvaluationIllegalArgumentException> {
			AddEvaluationParamsValidator().validate(validParams(type = null))
		}

		assertEquals(AddEvaluationUseCaseError.TypeMissed, exception.error)
	}

	private fun validParams(
		subjectId: String? = "subject-1",
		type: EvaluationType? = EvaluationType.TEST,
		maxGrade: Double? = 100.0
	): AddEvaluationParams {
		return AddEvaluationParams(
			subjectId = subjectId,
			subjectCode = "MA1111",
			quarterId = "quarter-1",
			type = type,
			scheduleMode = EvaluationScheduleMode.DATED,
			date = 1_700_000_000_000,
			grade = 80.0,
			maxGrade = maxGrade
		)
	}
}
