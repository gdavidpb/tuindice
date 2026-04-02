package com.gdavidpb.tuindice.record.data.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.QuarterWithSubjects
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteQuarter

fun RemoteQuarter.toLocalQuarter() = LocalQuarter(
	id = id,
	name = name,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	creditsSum = creditsSum,
	simulationGrade = simulationGrade,
	simulationGradeSum = simulationGradeSum,
	simulationCredits = simulationCredits,
	simulationCreditsSum = simulationCreditsSum,
	isCurrent = isCurrent,
	isReadOnly = isReadOnly,
	revision = revision,
	subjects = subjects.map { subject -> subject.toLocalSubject() }
)

fun LocalQuarter.toQuarterEntity() = QuarterEntity(
	id = id,
	name = name,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	creditsSum = creditsSum,
	simulationGrade = simulationGrade,
	simulationGradeSum = simulationGradeSum,
	simulationCredits = simulationCredits,
	simulationCreditsSum = simulationCreditsSum,
	isCurrent = isCurrent,
	isReadOnly = isReadOnly,
	revision = revision
)

fun QuarterWithSubjects.toLocalQuarter(): LocalQuarter {
	return LocalQuarter(
		id = quarter.id,
		name = quarter.name,
		startDate = quarter.startDate,
		endDate = quarter.endDate,
		grade = quarter.grade,
		gradeSum = quarter.gradeSum,
		credits = quarter.credits,
		creditsSum = quarter.creditsSum,
		simulationGrade = quarter.simulationGrade,
		simulationGradeSum = quarter.simulationGradeSum,
		simulationCredits = quarter.simulationCredits,
		simulationCreditsSum = quarter.simulationCreditsSum,
		isCurrent = quarter.isCurrent,
		isReadOnly = quarter.isReadOnly,
		revision = quarter.revision,
		subjects = subjects.map { subject -> subject.toLocalSubject() }
	)
}

fun LocalQuarter.toQuarter() = Quarter(
	id = id,
	name = name,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	creditsSum = creditsSum,
	simulationGrade = simulationGrade,
	simulationGradeSum = simulationGradeSum,
	simulationCredits = simulationCredits,
	simulationCreditsSum = simulationCreditsSum,
	isCurrent = isCurrent,
	isReadOnly = isReadOnly,
	subjects = subjects.map { subject -> subject.toSubject() }
)

fun Quarter.toLocalQuarter() = LocalQuarter(
	id = id,
	name = name,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	creditsSum = creditsSum,
	simulationGrade = simulationGrade,
	simulationGradeSum = simulationGradeSum,
	simulationCredits = simulationCredits,
	simulationCreditsSum = simulationCreditsSum,
	isCurrent = isCurrent,
	isReadOnly = isReadOnly,
	revision = 0L,
	subjects = subjects.map { subject -> subject.toLocalSubject() }
)
