package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams

fun RemoveQuarterParams.toQuarterRemove() = QuarterRemove(
	id = quarterId
)

fun SetSubjectGradeParams.toSubjectGradeSet() = SubjectGradeSet(
	id = subjectId,
	quarterId = quarterId,
	grade = grade,
	commit = commit
)