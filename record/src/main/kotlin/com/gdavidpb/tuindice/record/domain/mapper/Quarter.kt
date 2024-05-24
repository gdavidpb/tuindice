package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams

fun RemoveQuarterParams.toQuarterRemove() = QuarterRemove(
	id = quarterId
)

fun UpdateQuarterParams.toQuarterUpdate() = QuarterUpdate(
	id = quarterId,
	subjectsUpdates = subjectsUpdates,
	dispatchToRemote = dispatchToRemote
)

fun WithdrawSubjectParams.toQuarterUpdate() = QuarterUpdate(
	id = quarterId,
	subjectsUpdates = listOf(
		SubjectUpdate(
			id = subjectId,
			grade = MIN_SUBJECT_GRADE
		)
	),
	dispatchToRemote = true
)