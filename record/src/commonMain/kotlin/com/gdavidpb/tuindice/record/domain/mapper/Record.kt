package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.record.domain.model.QuarterAdd
import com.gdavidpb.tuindice.record.domain.model.QuarterAddSubject
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.usecase.param.AddQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.AddQuarterSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams

fun AddQuarterParams.toQuarterAdd() = QuarterAdd(
	quarter = quarter,
	year = year,
	subjects = subjects.map { subject -> subject.toQuarterAddSubject() }
)

fun AddQuarterSubjectParams.toQuarterAddSubject() = QuarterAddSubject(
	code = code,
	grade = grade
)

fun RemoveQuarterParams.toQuarterRemove() = QuarterRemove(
	id = quarterId
)

fun SetSubjectGradeParams.toSubjectGradeSet() = SubjectGradeSet(
	id = subjectId,
	quarterId = quarterId,
	grade = grade,
	status = status,
	commit = commit
)
