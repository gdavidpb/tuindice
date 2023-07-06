package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams

fun UpdateSubjectParams.toSubjectUpdate() = SubjectUpdate(
	subjectId = subjectId,
	grade = grade,
	dispatchToRemote = dispatchToRemote
)

fun WithdrawSubjectParams.toSubjectUpdate() = SubjectUpdate(
	subjectId = subjectId,
	grade = MIN_SUBJECT_GRADE,
	dispatchToRemote = true
)