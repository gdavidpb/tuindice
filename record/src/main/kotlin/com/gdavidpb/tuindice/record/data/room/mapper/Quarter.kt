package com.gdavidpb.tuindice.record.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.QuarterWithSubjects

fun Quarter.toQuarterEntity(uid: String) = QuarterEntity(
	id = id,
	name = name,
	accountId = uid,
	status = status,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits
)

fun QuarterEntity.toQuarter(subjects: List<Subject>): Quarter {
	val isEditable = (status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK)
	val isRetired = (status == STATUS_QUARTER_RETIRED)

	return Quarter(
		id = id,
		name = name,
		status = status,
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		isEditable = isEditable,
		isRetired = isRetired,
		subjects = subjects.toMutableList()
	)
}

@JvmName("toQuarterFromQuarterEntity")
fun QuarterEntity.toQuarter(subjects: List<SubjectEntity>): Quarter {
	val isEditable = (status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK)

	return toQuarter(subjects.map { subjectEntity -> subjectEntity.toSubject(isEditable) })
}

@JvmName("toQuarterFromQuarterWithSubjects")
fun QuarterWithSubjects.toQuarter() = quarter.toQuarter(subjects)