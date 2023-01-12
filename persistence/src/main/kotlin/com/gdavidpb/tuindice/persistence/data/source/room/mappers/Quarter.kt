package com.gdavidpb.tuindice.persistence.data.source.room.mappers

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.extensions.formatQuarterName
import com.gdavidpb.tuindice.persistence.data.source.room.entities.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.source.room.entities.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.source.room.otm.QuarterWithSubjects

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

@JvmName("toQuarterFromQuarterEntity")
fun QuarterEntity.toQuarter(subjects: List<SubjectEntity>) =
	toQuarter(subjects.map { subjectEntity -> subjectEntity.toSubject() })

@JvmName("toQuarterFromQuarterWithSubjects")
fun QuarterWithSubjects.toQuarter() = quarter.toQuarter(subjects)