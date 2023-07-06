package com.gdavidpb.tuindice.record.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectError
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams

class UpdateSubjectParamsValidator : ParamsValidator<UpdateSubjectParams> {
	override fun validate(params: UpdateSubjectParams) {
		require(params.grade in MIN_SUBJECT_GRADE..MAX_SUBJECT_GRADE) {
			throw SubjectIllegalArgumentException(SubjectError.OutOfRangeGrade)
		}
	}
}