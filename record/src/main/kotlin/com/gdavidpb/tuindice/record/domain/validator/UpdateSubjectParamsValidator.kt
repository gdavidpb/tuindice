package com.gdavidpb.tuindice.record.domain.validator

import com.gdavidpb.tuindice.base.domain.validator.Validator
import com.gdavidpb.tuindice.record.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.error.SubjectError
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams

class UpdateSubjectParamsValidator : Validator<UpdateSubjectParams> {
	override fun validate(params: UpdateSubjectParams) {
		require(params.grade in MIN_SUBJECT_GRADE..MAX_SUBJECT_GRADE) {
			throw SubjectIllegalArgumentException(SubjectError.OutOfRangeGrade)
		}
	}
}