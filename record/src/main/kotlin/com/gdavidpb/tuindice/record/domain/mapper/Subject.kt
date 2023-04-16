package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams

fun UpdateSubjectParams.toSubjectUpdate() = SubjectUpdate(
	subjectId = subjectId,
	grade = grade,
	dispatchToRemote = dispatchToRemote
)