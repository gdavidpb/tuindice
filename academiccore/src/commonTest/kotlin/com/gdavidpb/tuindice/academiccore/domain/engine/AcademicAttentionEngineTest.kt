package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttention
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcademicAttentionEngineTest {
	private val today = dateOf(15)

	private fun dateOf(day: Int, month: Int = 10) = LocalDate(2026, month, day)

	private fun attempt(
		code: String,
		enrollmentErrors: List<String>? = null,
		withdrawn: Boolean = false
	) = AcademicAttempt(
		id = "attempt-$code",
		subjectCode = code,
		subjectName = "Materia $code",
		credits = 3,
		gradingMode = AttemptGradingMode.NUMERIC,
		enrollmentErrors = enrollmentErrors,
		withdrawn = withdrawn
	)

	// A date is the start of its day in UTC: the tests read it back in UTC unless they say another zone.
	private fun evaluation(
		id: String,
		code: String,
		date: LocalDate?,
		grade: Double? = null
	) = Evaluation(
		id = id,
		attemptId = "attempt-$code",
		subjectCode = code,
		termId = "term-2026-SEP_DEC",
		scheduleMode = EvaluationScheduleMode.DATED,
		grade = grade,
		maxGrade = 20.0,
		date = date?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
		type = EvaluationType.TEST,
		state = EvaluationState.PENDING
	)

	private fun attentionOf(
		attempts: List<AcademicAttempt> = emptyList(),
		evaluations: List<Evaluation> = emptyList(),
		today: LocalDate = this.today,
		timeZone: TimeZone = TimeZone.UTC
	) = AcademicAttentionEngine.attentionOf(
		term = AcademicTerm(
			id = "term-2026-SEP_DEC",
			periodYear = 2026,
			periodCode = AcademicTermPeriod.SEP_DEC,
			kind = TermKind.CURRENT,
			attempts = attempts
		),
		evaluations = evaluations,
		today = today,
		timeZone = timeZone
	)

	@Test
	fun withNothingToAttendTo_thereIsNoAttention() {
		val evaluations = listOf(
			evaluation(id = "graded", code = "CI5437", date = dateOf(1), grade = 10.0),
			evaluation(id = "future", code = "CI5437", date = dateOf(30))
		)

		assertTrue(attentionOf(evaluations = evaluations).isEmpty())
	}

	@Test
	fun oneOverdueEvaluation_isOneUngradedPoint_onTheWeekItFellIn() {
		val evaluations = listOf(evaluation(id = "late", code = "CI5437", date = dateOf(8)))

		assertEquals(
			listOf(
				AcademicAttention.UngradedEvaluations(
					count = 1,
					oldestWeekStart = dateOf(5),
					oldestEvaluationId = "late"
				)
			),
			attentionOf(evaluations = evaluations)
		)
	}

	@Test
	fun severalOverdueEvaluations_areOnePoint_onTheWeekOfTheOldest() {
		val evaluations = listOf(
			evaluation(id = "recent", code = "CI5437", date = dateOf(14)),
			evaluation(id = "oldest", code = "CI5437", date = dateOf(7)),
			evaluation(id = "middle", code = "CI5437", date = dateOf(9)),
			evaluation(id = "today", code = "CI5437", date = today),
			evaluation(id = "graded", code = "CI5437", date = dateOf(1), grade = 12.0),
			evaluation(id = "continuous", code = "CI5437", date = dateOf(1))
				.copy(scheduleMode = EvaluationScheduleMode.CONTINUOUS)
		)

		assertEquals(
			listOf(
				AcademicAttention.UngradedEvaluations(
					count = 3,
					oldestWeekStart = dateOf(5),
					oldestEvaluationId = "oldest"
				)
			),
			attentionOf(evaluations = evaluations)
		)
	}

	@Test
	fun aTieOnTheDay_takesTheOldestBySubjectCode_andTheJumpLandsOnThatEvaluation() {
		val evaluations = listOf(
			evaluation(id = "later-code", code = "CI5437", date = dateOf(7)),
			evaluation(id = "earlier-code", code = "CI5311", date = dateOf(7))
		)

		assertEquals(
			listOf(
				AcademicAttention.UngradedEvaluations(
					count = 2,
					oldestWeekStart = dateOf(5),
					oldestEvaluationId = "earlier-code"
				)
			),
			attentionOf(attempts = listOf(attempt("CI5437"), attempt("CI5311")), evaluations = evaluations)
		)
	}

	@Test
	fun aSubjectWithAnEnrollmentError_isAPoint_withWhatTheUniversityPrinted() {
		val clash = attempt("CI5311", enrollmentErrors = listOf("CHOQUE DE HORARIO", "OTRO"))

		assertEquals(
			listOf(
				AcademicAttention.EnrollmentError(
					subjectCode = "CI5311",
					error = "CHOQUE DE HORARIO",
					attemptId = "attempt-CI5311"
				)
			),
			attentionOf(attempts = listOf(clash))
		)
	}

	@Test
	fun blankErrorsAndWithdrawnSubjects_areNotPoints() {
		val blank = attempt("CI5311", enrollmentErrors = listOf("  ", ""))
		val withdrawn = attempt("MA1111", enrollmentErrors = listOf("CHOQUE"), withdrawn = true)

		assertTrue(attentionOf(attempts = listOf(blank, withdrawn)).isEmpty())
	}

	@Test
	fun theAttention_isOrdered_andHasThreeAtMost() {
		val attempts = listOf(
			attempt("MA1111", enrollmentErrors = listOf("CHOQUE DE HORARIO")),
			attempt("CI5311", enrollmentErrors = listOf("CHOQUE DE HORARIO")),
			attempt("EG1114", enrollmentErrors = listOf("SIN CUPO")),
			attempt("FS1111", enrollmentErrors = listOf("SIN CUPO"))
		)
		val evaluations = listOf(evaluation(id = "late", code = "CI5311", date = dateOf(8)))

		val attention = attentionOf(attempts = attempts, evaluations = evaluations)

		assertEquals(3, attention.size)
		assertTrue(attention[0] is AcademicAttention.UngradedEvaluations)
		assertEquals(
			listOf("CI5311", "EG1114"),
			attention.drop(1).map { (it as AcademicAttention.EnrollmentError).subjectCode }
		)
	}

	@Test
	fun aDateNearMidnight_isAnotherDay_inAnotherZone_andSoAnotherPoint() {
		// 02:30 UTC on the 11th is 22:30 on the 10th in Caracas (UTC-4): there the evaluation is a day gone,
		// and in UTC it falls today, so it is not a point.
		val lateAtNight = evaluation(id = "late", code = "CI5437", date = null).copy(
			date = dateOf(11).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() + 150 * 60_000L
		)
		val evaluations = listOf(lateAtNight)

		assertEquals(
			listOf(
				AcademicAttention.UngradedEvaluations(
					count = 1,
					oldestWeekStart = dateOf(5),
					oldestEvaluationId = "late"
				)
			),
			attentionOf(evaluations = evaluations, today = dateOf(11), timeZone = TimeZone.of("America/Caracas"))
		)
		assertTrue(attentionOf(evaluations = evaluations, today = dateOf(11), timeZone = TimeZone.UTC).isEmpty())
	}
}
