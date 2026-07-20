package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AttemptItemMappingUiTest {
	@Test
	fun when_attemptIsGradedNumeric_then_buildsTextsAndColors() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				score = AttemptScore.numeric(4),
				outcome = AttemptOutcome.APPROVED
			).toAttemptItem(
				isReadOnly = false,
				texts = mapperTexts()
			)
		}

		val mapped = assertNotNull(item)
		assertEquals("attempt-1", mapped.attemptId)
		assertEquals("MA1112", mapped.subjectCode)
		assertEquals(4, mapped.grade)
		assertEquals(GradingMode.NUMERIC, mapped.gradingMode)
		assertEquals(AttemptOutcome.APPROVED, mapped.outcome)
		assertEquals("MA1112", mapped.codeText)
		assertEquals("MATERIA UNO", mapped.nameText)
		assertEquals("Nota 4", mapped.gradeText)
		assertEquals("4 UC", mapped.creditsText)
		assertEquals(CourseCodeColorGenerator.fromCode("MA1112").color, mapped.codeColor)
		assertEquals(
			CourseCodeColorGenerator.fromCode("MA1112").containerColor,
			mapped.codeContainerColor
		)
		assertEquals(false, mapped.isReadOnly)
	}

	@Test
	fun when_attemptIsRetired_then_hidesGradeText() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				score = AttemptScore.numeric(2),
				outcome = AttemptOutcome.RETIRED
			).toAttemptItem(
				isReadOnly = false,
				texts = mapperTexts()
			)
		}

		val mapped = assertNotNull(item)
		assertEquals("", mapped.gradeText)
		assertEquals(AttemptOutcome.RETIRED, mapped.outcome)
	}

	@Test
	fun when_attemptOutcomeIsPending_then_resolvesToNullAndHidesGradeText() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				score = AttemptScore.empty(),
				outcome = AttemptOutcome.PENDING
			).toAttemptItem(
				isReadOnly = false,
				texts = mapperTexts()
			)
		}

		val mapped = assertNotNull(item)
		assertNull(mapped.outcome)
		assertEquals(0, mapped.grade)
		assertEquals("", mapped.gradeText)
	}

	@Test
	fun when_attemptIsUnreported_then_showsMinimumGrade() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				score = AttemptScore.empty(),
				outcome = AttemptOutcome.UNREPORTED
			).toAttemptItem(
				isReadOnly = false,
				texts = mapperTexts()
			)
		}

		val mapped = assertNotNull(item)
		assertEquals(AttemptOutcome.UNREPORTED, mapped.outcome)
		assertEquals("Nota 0", mapped.gradeText)
	}

	@Test
	fun when_attemptIsQualitative_then_hidesGradeText() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
				score = AttemptScore.symbolic("PA"),
				outcome = AttemptOutcome.APPROVED
			).toAttemptItem(
				isReadOnly = false,
				texts = mapperTexts()
			)
		}

		val mapped = assertNotNull(item)
		assertEquals(GradingMode.QUALITATIVE_PASS_FAIL, mapped.gradingMode)
		assertEquals("", mapped.gradeText)
	}

	@Test
	fun when_attemptItemIsMapped_then_passesThroughReadOnlyFlagAndBadge() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				score = AttemptScore.numeric(3),
				outcome = AttemptOutcome.APPROVED,
				badge = AttemptBadge.WITHOUT_EFFECT
			).toAttemptItem(
				isReadOnly = true,
				texts = mapperTexts()
			)
		}

		val mapped = assertNotNull(item)
		assertTrue(mapped.isReadOnly)
		assertEquals(AttemptBadge.WITHOUT_EFFECT, mapped.badge)
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

	private fun attemptProjection(
		gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC,
		score: AttemptScore = AttemptScore.empty(),
		outcome: AttemptOutcome = AttemptOutcome.PENDING,
		badge: AttemptBadge = AttemptBadge.NONE
	): AttemptProjection {
		return AttemptProjection(
			id = "attempt-1",
			subjectCode = "MA1112",
			subjectName = "materia uno",
			credits = 4,
			gradingMode = gradingMode,
			score = score,
			outcome = outcome,
			badge = badge
		)
	}
}
