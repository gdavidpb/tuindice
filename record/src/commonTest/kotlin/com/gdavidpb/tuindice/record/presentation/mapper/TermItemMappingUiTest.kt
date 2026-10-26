package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import com.gdavidpb.tuindice.record.ui.view.TermSummaryContent
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TermItemMappingUiTest {
	@Test
	fun when_termIsSyntheticInProjectionMode_then_buildsHeaderTextsAndFlags() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			val mapped = termProjection(
				periodAverage = 4.25,
				cumulativeAverage = 3.5,
				periodCredits = 12
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts()
			)
			item = mapped
			TermSummaryContent(item = mapped, texts = mapperTexts())
		}

		val mapped = assertNotNull(item)
		assertEquals("term-1", mapped.termId)
		assertEquals(2027, mapped.periodYear)
		assertEquals(20276, mapped.termOrder)
		assertEquals("Sep - Dic 2027", mapped.shortNameText)
		assertEquals(TermItemKind.SYNTHETIC, mapped.kind)
		assertFalse(mapped.isCurrent)
		assertTrue(mapped.canDelete)
		assertTrue(mapped.canEdit)
		assertEquals(4.25f, mapped.gradeTarget)
		assertEquals(3.5f, mapped.gradeSumTarget)
		assertEquals(12, mapped.creditsTarget)
		assertNull(mapped.previousGradeTarget)
		assertNull(mapped.previousGradeSumTarget)
		assertNull(mapped.previousCreditsTarget)
		assertFalse(mapped.attempts.single().isReadOnly)

		// What the summary shows of them, with no change under any of them.
		onNodeWithText("4.25 prom").assertIsDisplayed()
		onNodeWithText("3.5 acum").assertIsDisplayed()
		onNodeWithText("12 UC").assertIsDisplayed()
		onAllNodesWithText("▲", substring = true).assertCountEquals(0)
		onAllNodesWithText("▼", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_termHasOlderNeighbor_then_computesMetricDeltas() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			val mapped = termProjection(
				periodAverage = 4.0,
				cumulativeAverage = 4.0,
				periodCredits = 12
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts(),
				previousTerm = termProjection(
					id = "term-0",
					periodYear = 2026,
					periodAverage = 3.5,
					cumulativeAverage = 4.0,
					periodCredits = 15
				)
			)
			item = mapped
			TermSummaryContent(item = mapped, texts = mapperTexts())
		}

		val mapped = assertNotNull(item)
		// Each change is measured from the older term's own value.
		assertEquals(3.5f, mapped.previousGradeTarget)
		assertEquals(4.0f, mapped.previousGradeSumTarget)
		assertEquals(15, mapped.previousCreditsTarget)

		onNodeWithText("▲ 0.5000").assertIsDisplayed()
		onNodeWithText("0.0000").assertIsDisplayed()
		onNodeWithText("▼ 3").assertIsDisplayed()
		// Each change takes the tone its sign and size give it.
		assertEquals(TermMetricDeltaTone.Positive, 0.5f.toTermMetricDelta().tone)
		assertEquals(TermMetricDeltaTone.Neutral, 0f.toTermMetricDelta().tone)
		assertEquals(TermMetricDeltaTone.Informational, (-3).toCreditsTermMetricDelta().tone)
	}

	@Test
	fun when_deltasAreNegativeOrZero_then_marksThemWithExpectedTexts() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			val mapped = termProjection(
				periodAverage = 3.0,
				cumulativeAverage = 3.5,
				periodCredits = 12
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts(),
				previousTerm = termProjection(
					id = "term-0",
					periodYear = 2026,
					periodAverage = 3.5,
					cumulativeAverage = 3.5,
					periodCredits = 12
				)
			)
			item = mapped
			TermSummaryContent(item = mapped, texts = mapperTexts())
		}

		val mapped = assertNotNull(item)
		assertEquals(3.5f, mapped.previousGradeTarget)
		assertEquals(12, mapped.previousCreditsTarget)

		onNodeWithText("▼ 0.5000").assertIsDisplayed()
		onNodeWithText("0").assertIsDisplayed()
		assertEquals(TermMetricDeltaTone.Negative, (-0.5f).toTermMetricDelta().tone)
		assertEquals(TermMetricDeltaTone.Informational, 0.toCreditsTermMetricDelta().tone)
	}

	@Test
	fun when_termHasNoAttempts_then_skipsDeltas() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			val mapped = termProjection(
				attempts = emptyList()
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts(),
				previousTerm = termProjection(id = "term-0", periodYear = 2026)
			)
			item = mapped
			TermSummaryContent(item = mapped, texts = mapperTexts())
		}

		val mapped = assertNotNull(item)
		assertNull(mapped.previousGradeTarget)
		assertNull(mapped.previousGradeSumTarget)
		assertNull(mapped.previousCreditsTarget)
		onAllNodesWithText("▲", substring = true).assertCountEquals(0)
		onAllNodesWithText("▼", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_currentTermIsInHistoricalMode_then_flagsItReadOnly() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(
				kind = TermKind.CURRENT
			).toTermItem(
				viewMode = RecordViewMode.Historical,
				texts = mapperTexts()
			)
		}

		val mapped = assertNotNull(item)
		assertEquals(TermItemKind.CURRENT, mapped.kind)
		assertTrue(mapped.isCurrent)
		assertFalse(mapped.canDelete)
		assertFalse(mapped.canEdit)
		assertTrue(mapped.attempts.single().isReadOnly)
	}

	@Test
	fun when_termListIsMapped_then_pairsEachTermWithItsOlderNeighbor() = runTuIndiceUiTest {
		var items: List<TermItem>? = null

		setTuIndiceTestContent {
			val mapped = listOf(
				termProjection(
					id = "new",
					periodYear = 2027,
					periodAverage = 4.0
				),
				termProjection(
					id = "old",
					periodYear = 2026,
					periodAverage = 3.5
				)
			).toTermItemList(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts()
			)
			items = mapped
			TermSummaryContent(item = mapped.first(), texts = mapperTexts())
		}

		val mapped = assertNotNull(items)
		assertEquals(listOf("new", "old"), mapped.map(TermItem::termId))

		assertEquals(3.5f, mapped.first().previousGradeTarget)
		assertNull(mapped.last().previousGradeTarget)
		onNodeWithText("▲ 0.5000").assertIsDisplayed()
	}

	private fun mapperTexts(): RecordMapperTexts {
		return RecordMapperTexts(
			termGrade = { value -> "$value prom" },
			termGradeSum = { value -> "$value acum" },
			termCredits = { credits -> "$credits UC" },
			termAttemptGrade = { grade -> "Nota $grade" },
			termAttemptCredits = { credits -> "$credits UC" },
			termAttemptSection = { section -> "Sección $section" },
			termAttemptSectionClassroom = { section, classroom -> "Sección $section · $classroom" }
		)
	}

	private fun termProjection(
		id: String = "term-1",
		kind: TermKind = TermKind.SYNTHETIC,
		periodYear: Int = 2027,
		periodCode: AcademicTermPeriod = AcademicTermPeriod.SEP_DEC,
		periodAverage: Double = 4.0,
		cumulativeAverage: Double = 4.0,
		periodCredits: Int = 12,
		cumulativeCredits: Int = 40,
		attempts: List<AttemptProjection> = listOf(attemptProjection())
	): TermProjection {
		return TermProjection(
			id = id,
			periodYear = periodYear,
			periodCode = periodCode,
			termKey = "$periodYear-${periodCode.name}",
			termOrder = periodYear * 10 + periodCode.sequence,
			periodLabel = "${periodCode.label} $periodYear",
			kind = kind,
			periodAverage = periodAverage,
			cumulativeAverage = cumulativeAverage,
			periodCredits = periodCredits,
			cumulativeCredits = cumulativeCredits,
			attempts = attempts
		)
	}

	private fun attemptProjection(): AttemptProjection {
		return AttemptProjection(
			id = "attempt-1",
			subjectCode = "MA1112",
			subjectName = "materia uno",
			credits = 4,
			gradingMode = AttemptGradingMode.NUMERIC,
			score = AttemptScore.numeric(4),
			outcome = AttemptOutcome.APPROVED,
			badge = AttemptBadge.NONE
		)
	}
}
