package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

// The days of the hub's mocks: Thursday 15/10/2026 is today, in week 3 of a term whose classes
// started on Monday 28/09/2026. The model's days of the week are 1 = Sunday ... 7 = Saturday.
const val HUB_THURSDAY = 5
const val HUB_MONDAY = 2
const val HUB_WEDNESDAY = 4
const val HUB_FRIDAY = 6

const val HUB_CLASSES_START = "2026-09-28"

fun hubDate(day: Int, month: Int = 10) = LocalDate(2026, month, day)

fun hubAttempt(
	code: String,
	schedule: List<AcademicScheduleEntry>? = null,
	enrollmentErrors: List<String>? = null,
	withdrawn: Boolean = false,
	gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC
) = AcademicAttempt(
	id = "attempt-$code",
	subjectCode = code,
	subjectName = "Materia $code",
	credits = 3,
	gradingMode = gradingMode,
	schedule = schedule,
	enrollmentErrors = enrollmentErrors,
	withdrawn = withdrawn
)

fun hubMeeting(day: Int, start: Int, end: Int = start, classroom: String = "MYS-210") =
	AcademicScheduleEntry(dayOfWeek = day, startBlock = start, endBlock = end, classroom = classroom)

fun hubTerm(
	attempts: List<AcademicAttempt>,
	kind: TermKind = TermKind.CURRENT,
	periodCode: AcademicTermPeriod = AcademicTermPeriod.SEP_DEC,
	classesStartDate: String? = HUB_CLASSES_START
) = AcademicTerm(
	id = "term-2026-${periodCode.name}",
	periodYear = 2026,
	periodCode = periodCode,
	kind = kind,
	attempts = attempts,
	classesStartDate = classesStartDate
)

fun hubRecord(vararg terms: AcademicTerm) = AcademicRecord(id = "record", terms = terms.toList())

fun hubEvaluation(
	id: String,
	code: String,
	date: LocalDate?,
	grade: Double? = null,
	type: EvaluationType = EvaluationType.TEST
) = Evaluation(
	id = id,
	attemptId = "attempt-$code",
	subjectCode = code,
	termId = "term-2026-SEP_DEC",
	scheduleMode = EvaluationScheduleMode.DATED,
	grade = grade,
	maxGrade = 20.0,
	date = date?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
	type = type,
	// What the repository computed when it mapped the evaluation; the hub tells it again for today.
	state = EvaluationState.PENDING
)
