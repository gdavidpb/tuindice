package com.gdavidpb.tuindice.evaluations.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AddEvaluationParamsValidatorTest {
	@Test
	fun validate_whenRequiredFieldsArePresent_doesNotThrow() {
		AddEvaluationParamsValidator().validate(
			AddEvaluationParams(
				subjectId = "s1",
				subjectCode = "MA1111",
				quarterId = "q1",
				type = EvaluationType.TEST,
				date = 1_735_700_000_000L,
				grade = 18.0,
				maxGrade = 20.0
			)
		)
	}

	@Test
	fun validate_whenSubjectIsMissing_returnsSubjectMissedError() {
		val error = assertFailsWith<AddEvaluationIllegalArgumentException> {
			AddEvaluationParamsValidator().validate(
				AddEvaluationParams(
					subjectId = null,
					subjectCode = "MA1111",
					quarterId = "q1",
					type = EvaluationType.TEST,
					date = null,
					grade = null,
					maxGrade = 20.0
				)
			)
		}

		assertEquals(AddEvaluationUseCaseError.SubjectMissed, error.error)
	}

	@Test
	fun validate_whenMaxGradeIsMissing_returnsMaxGradeMissedError() {
		val error = assertFailsWith<AddEvaluationIllegalArgumentException> {
			AddEvaluationParamsValidator().validate(
				AddEvaluationParams(
					subjectId = "s1",
					subjectCode = "MA1111",
					quarterId = "q1",
					type = EvaluationType.TEST,
					date = null,
					grade = null,
					maxGrade = null
				)
			)
		}

		assertEquals(AddEvaluationUseCaseError.MaxGradeMissed, error.error)
	}

	@Test
	fun validate_whenTypeIsMissing_returnsTypeMissedError() {
		val error = assertFailsWith<AddEvaluationIllegalArgumentException> {
			AddEvaluationParamsValidator().validate(
				AddEvaluationParams(
					subjectId = "s1",
					subjectCode = "MA1111",
					quarterId = "q1",
					type = null,
					date = null,
					grade = null,
					maxGrade = 20.0
				)
			)
		}

		assertEquals(AddEvaluationUseCaseError.TypeMissed, error.error)
	}
}
