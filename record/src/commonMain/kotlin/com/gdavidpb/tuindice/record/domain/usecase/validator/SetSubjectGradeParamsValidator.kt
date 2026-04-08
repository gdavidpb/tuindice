package com.gdavidpb.tuindice.record.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams

class SetSubjectGradeParamsValidator : ParamsValidator<SetSubjectGradeParams> {
	override fun validate(params: SetSubjectGradeParams) {
		require((params.grade != null) || (params.status != null)) {
			throw SubjectIllegalArgumentException(SubjectUseCaseError.OutOfRangeGrade)
		}

		params.grade?.let { grade ->
			require(grade in MIN_SUBJECT_GRADE..MAX_SUBJECT_GRADE) {
				throw SubjectIllegalArgumentException(SubjectUseCaseError.OutOfRangeGrade)
			}
		}

		params.status?.let { status ->
			require(
				status in setOf(
					SubjectStatus.NORMAL,
					SubjectStatus.APPROVED,
					SubjectStatus.FAILED,
					SubjectStatus.RETIRED
				)
			) {
				throw SubjectIllegalArgumentException(SubjectUseCaseError.OutOfRangeGrade)
			}
		}
	}
}
