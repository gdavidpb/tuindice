package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.presentation.contract.Record

fun (Record.Action.UpdateSubject).toUpdateSubjectParams() = UpdateSubjectParams(
	quarterId = quarterId,
	subjectId = subjectId,
	grade = grade,
	dispatchToRemote = dispatchToRemote
)