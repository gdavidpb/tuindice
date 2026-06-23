package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
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
	fun toTermItem_buildsHeaderTextsAndFlags_forSyntheticTermInProjectionMode() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(
				periodAverage = 4.25,
				cumulativeAverage = 3.5,
				periodCredits = 12
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts(),
				highlightColor = Color.Red
			)
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
		assertEquals("4.25 prom", mapped.gradeText.text)
		assertEquals("3.5 acum", mapped.gradeSumText.text)
		assertEquals("12 UC", mapped.creditsText.text)
		assertNull(mapped.gradeDelta)
		assertNull(mapped.gradeSumDelta)
		assertNull(mapped.creditsDelta)
		assertFalse(mapped.attempts.single().isReadOnly)
	}

	@Test
	fun toTermItem_computesMetricDeltas_againstOlderTerm() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(
				periodAverage = 4.0,
				cumulativeAverage = 4.0,
				periodCredits = 12
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts(),
				highlightColor = Color.Red,
				previousTerm = termProjection(
					id = "term-0",
					periodYear = 2026,
					periodAverage = 3.5,
					cumulativeAverage = 4.0,
					periodCredits = 15
				)
			)
		}

		val mapped = assertNotNull(item)

		val gradeDelta = assertNotNull(mapped.gradeDelta)
		assertEquals("▲ 0.5000", gradeDelta.text)
		assertEquals(TermMetricDeltaTone.Positive, gradeDelta.tone)

		val gradeSumDelta = assertNotNull(mapped.gradeSumDelta)
		assertEquals("0.0000", gradeSumDelta.text)
		assertEquals(TermMetricDeltaTone.Neutral, gradeSumDelta.tone)

		val creditsDelta = assertNotNull(mapped.creditsDelta)
		assertEquals("▼ 3", creditsDelta.text)
		assertEquals(TermMetricDeltaTone.Informational, creditsDelta.tone)
	}

	@Test
	fun toTermItem_marksNegativeAndZeroDeltas_withExpectedTexts() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(
				periodAverage = 3.0,
				cumulativeAverage = 3.5,
				periodCredits = 12
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts(),
				highlightColor = Color.Red,
				previousTerm = termProjection(
					id = "term-0",
					periodYear = 2026,
					periodAverage = 3.5,
					cumulativeAverage = 3.5,
					periodCredits = 12
				)
			)
		}

		val mapped = assertNotNull(item)

		val gradeDelta = assertNotNull(mapped.gradeDelta)
		assertEquals("▼ 0.5000", gradeDelta.text)
		assertEquals(TermMetricDeltaTone.Negative, gradeDelta.tone)

		val creditsDelta = assertNotNull(mapped.creditsDelta)
		assertEquals("0", creditsDelta.text)
		assertEquals(TermMetricDeltaTone.Informational, creditsDelta.tone)
	}

	@Test
	fun toTermItem_skipsDeltas_whenTermHasNoAttempts() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(
				attempts = emptyList()
			).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = mapperTexts(),
				highlightColor = Color.Red,
				previousTerm = termProjection(id = "term-0", periodYear = 2026)
			)
		}

		val mapped = assertNotNull(item)
		assertNull(mapped.gradeDelta)
		assertNull(mapped.gradeSumDelta)
		assertNull(mapped.creditsDelta)
	}

	@Test
	fun toTermItem_flagsCurrentTermReadOnly_inHistoricalMode() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(
				kind = TermKind.CURRENT
			).toTermItem(
				viewMode = RecordViewMode.Historical,
				texts = mapperTexts(),
				highlightColor = Color.Red
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
	fun toTermItemList_pairsEachTermWithItsOlderNeighbor() = runTuIndiceUiTest {
		var items: List<TermItem>? = null

		setTuIndiceTestContent {
			items = listOf(
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
				texts = mapperTexts(),
				highlightColor = Color.Red
			)
		}

		val mapped = assertNotNull(items)
		assertEquals(listOf("new", "old"), mapped.map(TermItem::termId))

		val newestDelta = assertNotNull(mapped.first().gradeDelta)
		assertEquals("▲ 0.5000", newestDelta.text)
		assertNull(mapped.last().gradeDelta)
	}

	private fun mapperTexts(): RecordMapperTexts {
		return RecordMapperTexts(
			termGrade = { value -> "$value prom" },
			termGradeSum = { value -> "$value acum" },
			termCredits = { credits -> "$credits UC" },
			termAttemptGrade = { grade -> "Nota $grade" },
			termAttemptCredits = { credits -> "$credits UC" }
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
