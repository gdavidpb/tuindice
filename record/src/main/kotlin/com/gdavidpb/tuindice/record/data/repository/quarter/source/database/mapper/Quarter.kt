package com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.base.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.QuarterWithSubjects
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate

fun LocalQuarter.toQuarterRemove() = QuarterRemove(
	id = id
)

fun LocalQuarter.toQuarterUpdate() = QuarterUpdate(
	id = id,
	subjectsUpdates = subjects.map { subject -> subject.toSubjectUpdate() },
	dispatchToRemote = false
)

fun RemoteQuarter.toLocalQuarter() = LocalQuarter(
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
	subjects = subjects.map { subject -> subject.toLocalSubject() }
)

fun LocalQuarter.toQuarterEntity(uid: String) = QuarterEntity(
	id = id,
	accountId = uid,
	name = name,
	status = status,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits
)

fun QuarterWithSubjects.toLocalQuarter(): LocalQuarter {
	val isEditable = (quarter.status == STATUS_QUARTER_CURRENT) ||
			(quarter.status == STATUS_QUARTER_MOCK)

	val isRetired = (quarter.status == STATUS_QUARTER_RETIRED)

	return LocalQuarter(
		id = quarter.id,
		name = quarter.name,
		status = quarter.status,
		startDate = quarter.startDate,
		endDate = quarter.endDate,
		grade = quarter.grade,
		gradeSum = quarter.gradeSum,
		credits = quarter.credits,
		isEditable = isEditable,
		isRetired = isRetired,
		subjects = subjects.map { subject -> subject.toLocalSubject(isEditable) }
	)
}

fun LocalQuarter.toQuarter() = Quarter(
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
	subjects = subjects.map { subject -> subject.toSubject() }
)

fun Quarter.toLocalQuarter() = LocalQuarter(
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
	subjects = subjects.map { subject -> subject.toLocalSubject() }
)