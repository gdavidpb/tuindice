package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateQuarterParams
import com.gdavidpb.tuindice.record.presentation.contract.Record

fun (Record.Action.UpdateSubject).toUpdateSubjectParams() = UpdateQuarterParams(
	quarterId = quarterId,
	subjectsUpdates = listOf(
		SubjectUpdate(id = subjectId, grade = grade)
	),
	dispatchToRemote = dispatchToRemote
)