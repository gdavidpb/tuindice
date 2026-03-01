package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RecordParamsValidatorTest {
	@Test
	fun setSubjectGradeParamsValidator_acceptsGradesInRange() {
		SetSubjectGradeParamsValidator().validate(
			SetSubjectGradeParams(
				quarterId = "quarter-1",
				subjectId = "subject-1",
				grade = MAX_SUBJECT_GRADE,
				commit = true
			)
		)
	}

	@Test
	fun setSubjectGradeParamsValidator_rejectsGradesOutOfRange() {
		val exception = assertFailsWith<SubjectIllegalArgumentException> {
			SetSubjectGradeParamsValidator().validate(
				SetSubjectGradeParams(
					quarterId = "quarter-1",
					subjectId = "subject-1",
					grade = MAX_SUBJECT_GRADE + 1,
					commit = true
				)
			)
		}

		assertEquals(SubjectUseCaseError.OutOfRangeGrade, exception.error)
	}
}
