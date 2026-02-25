package com.gdavidpb.tuindice.record.domain.usecase.validator

import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SetSubjectGradeParamsValidatorTest {
	@Test
	fun validate_whenGradeIsInRange_doesNotThrow() {
		val validator = SetSubjectGradeParamsValidator()

		validator.validate(
			SetSubjectGradeParams(
				quarterId = "q1",
				subjectId = "s1",
				grade = MIN_SUBJECT_GRADE,
				commit = false
			)
		)
		validator.validate(
			SetSubjectGradeParams(
				quarterId = "q1",
				subjectId = "s1",
				grade = MAX_SUBJECT_GRADE,
				commit = true
			)
		)
	}

	@Test
	fun validate_whenGradeIsOutOfRange_returnsOutOfRangeError() {
		val error = assertFailsWith<SubjectIllegalArgumentException> {
			SetSubjectGradeParamsValidator().validate(
				SetSubjectGradeParams(
					quarterId = "q1",
					subjectId = "s1",
					grade = MAX_SUBJECT_GRADE + 1,
					commit = true
				)
			)
		}

		assertEquals(SubjectUseCaseError.OutOfRangeGrade, error.error)
	}
}
