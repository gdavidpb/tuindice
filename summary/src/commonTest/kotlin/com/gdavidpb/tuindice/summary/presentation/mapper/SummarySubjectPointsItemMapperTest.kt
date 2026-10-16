package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationPoints
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_points_evaluated
import tuindice.summary.generated.resources.summary_points_no_evaluations
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_points_rest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SummarySubjectPointsItemMapperTest {
	private fun term(week: Int?, subjects: List<SummarySubjectPoints> = emptyList()) = SummaryTerm.InProgress(
		periodCode = AcademicTermPeriod.SEP_DEC,
		periodYear = 2026,
		academicWeek = week,
		subjects = subjects,
		hasSyncedEvaluations = true
	)

	private fun subject(code: String, points: EvaluationPoints?) = SummarySubjectPoints(
		subjectCode = code,
		points = points,
		attemptId = "attempt-$code",
		credits = 3
	)

	@Test
	fun thePoints_areEarnedOverEvaluated_withoutDecimalsWhenThereAreNone() {
		val subjects = listOf(
			subject("CI5311", EvaluationPoints(41.0, 60.0, 3, 4)),
			subject("CI5437", EvaluationPoints(18.5, 25.0, 2, 2)),
			subject("EG1114", points = null)
		)

		val items = term(week = 3, subjects = subjects).toSummaryTermItem().subjects

		assertEquals("41", items[0].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_rest, listOf("60")), items[0].restText)
		assertEquals("18.50", items[1].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_rest, listOf("25")), items[1].restText)
		assertNull(items[2].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_no_evaluations), items[2].restText)
	}

	@Test
	fun aSubjectWithEvaluationsButNoGrade_saysSinNotasAun_andOneWithNoEvaluationSaysSinEvaluaciones() {
		val items = term(
			week = 3,
			subjects = listOf(subject("CI5311", EvaluationPoints(0.0, 0.0, 0, 2)), subject("EG1114", points = null))
		).toSummaryTermItem().subjects

		assertNull(items[0].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_none), items[0].restText)
		assertEquals(UiText.Resource(Res.string.summary_points_no_evaluations), items[1].restText)
		// Its bar stays, empty, and the evaluated text is left out: "Sin notas aún" already says nothing is evaluated.
		assertNull(items[0].evaluatedText)
		assertEquals(0f, items[0].earnedFraction)
		assertEquals(0f, items[0].evaluatedFraction)
		// A subject with no evaluations has no bar at all.
		assertNull(items[1].evaluatedFraction)
		assertEquals(0f, items[1].earnedFraction)
	}

	@Test
	fun theEvaluatedText_isWhatIsEvaluatedOfOneHundred_andTheBarIsWhatIsEarnedAndEvaluatedOfIt() {
		val items = term(
			week = 3,
			subjects = listOf(
				subject("CI5311", EvaluationPoints(41.0, 60.0, 3, 4)),
				// Six graded evaluations of 20 make 120 evaluated: the text and the bar stop at the total.
				subject("CI5437", EvaluationPoints(100.0, 120.0, 6, 6)),
				subject("EG1114", points = null)
			)
		).toSummaryTermItem().subjects

		assertEquals(UiText.Resource(Res.string.summary_points_evaluated, listOf("60", "100")), items[0].evaluatedText)
		// Of the 60 evaluated, 41 are earned: the earned part is 41 of the 100 points, and the bar goes on to 60.
		assertEquals(0.41f, items[0].earnedFraction)
		assertEquals(0.6f, items[0].evaluatedFraction)
		assertEquals(UiText.Resource(Res.string.summary_points_evaluated, listOf("100", "100")), items[1].evaluatedText)
		assertEquals(1f, items[1].earnedFraction)
		assertEquals(1f, items[1].evaluatedFraction)
		// Nothing is registered for EG1114: no bar and no evaluated text at all, not "0 de 100".
		assertEquals(null, items[2].evaluatedText)
		assertEquals(0f, items[2].earnedFraction)
		assertNull(items[2].evaluatedFraction)
	}

	@Test
	fun theEarnedFraction_isHeldAtWhatIsEvaluated_evenWhenTheGradesSayMore() {
		// Earned past what is evaluated cannot come from valid grades, but if it did the bar would still stop at what is
		// evaluated.
		val items = term(
			week = 3,
			subjects = listOf(subject("CI5311", EvaluationPoints(25.0, 20.0, 1, 1)))
		).toSummaryTermItem().subjects

		assertEquals(0.2f, items.single().earnedFraction)
		assertEquals(0.2f, items.single().evaluatedFraction)
	}
}
