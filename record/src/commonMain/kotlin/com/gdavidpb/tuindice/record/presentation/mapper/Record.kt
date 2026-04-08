package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import com.gdavidpb.tuindice.record.presentation.contract.Record

fun (Record.Action.SetSubjectGrade).toSetSubjectGradeParams() = SetSubjectGradeParams(
	quarterId = quarterId,
	subjectId = subjectId,
	grade = grade,
	status = status,
	commit = commit
)
