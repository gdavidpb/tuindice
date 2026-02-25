package com.gdavidpb.tuindice.record.data.repository.quarter.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject

fun RemoteQuarter.toLocalQuarter() = LocalQuarter(
	id = id,
	name = name,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	creditsSum = creditsSum,
	isCurrent = isCurrent,
	isReadOnly = isReadOnly,
	subjects = subjects.map { subject -> subject.toLocalSubject() }
)

fun LocalQuarter.toQuarter() = Quarter(
	id = id,
	name = name,
	startDate = startDate,
	endDate = endDate,
	grade = grade,
	gradeSum = gradeSum,
	credits = credits,
	creditsSum = creditsSum,
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
	isCurrent = isCurrent,
	isReadOnly = isReadOnly,
	subjects = subjects.map { subject -> subject.toLocalSubject() }
)

fun Subject.toRemoteSubject() = RemoteSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade
)

fun RemoteSubject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade
)

fun LocalSubject.toSubject() = Subject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade
)

fun Subject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade
)
