package com.gdavidpb.tuindice.record.utils

import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.MIN_SUBJECT_GRADE

object Ranges {
	val subjectGrade = MIN_SUBJECT_GRADE.toFloat()..MAX_SUBJECT_GRADE.toFloat()
}
