package com.gdavidpb.tuindice.data.source.room.mappers

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.extensions.formatQuarterName
import com.gdavidpb.tuindice.data.source.room.entities.QuarterEntity

fun Quarter.toQuarterEntity(uid: String) = QuarterEntity(
	id = id,
	accountId = uid,
	status = status,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits
)

fun QuarterEntity.toQuarter(subjects: List<Subject>) = Quarter(
	id = id,
	name = (startDate to endDate).formatQuarterName(),
	status = status,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	subjects = subjects.toMutableList()
)