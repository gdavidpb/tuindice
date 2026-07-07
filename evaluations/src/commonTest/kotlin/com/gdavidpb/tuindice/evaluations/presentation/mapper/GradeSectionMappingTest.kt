package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GradeSectionMappingTest {
	@Test
	fun updated_whenGradesAreKnown_formatsThemWithTwoDecimals() {
		val updated = gradeSectionItem().updated(
			grade = 18.5,
			maxGrade = 20.0
		)

		assertEquals("18.50", updated.gradeText)
		assertEquals("20.00", updated.maxGradeText)
	}

	@Test
	fun updated_whenGradesAreMissing_fallsBackToZero() {
		val updated = gradeSectionItem().updated(
			grade = null,
			maxGrade = null
		)

		assertEquals("0.00", updated.gradeText)
		assertEquals("0.00", updated.maxGradeText)
	}

	// The editor mirrors the list row rule: any evaluation with a usable max grade
	// can take a grade, whether or not its date has passed.
	@Test
	fun updated_whenMaxGradeIsUsable_showsGradeChip() {
		val updated = gradeSectionItem().updated(
			grade = null,
			maxGrade = 20.0
		)

		assertTrue(updated.showsGradeChip)
	}

	@Test
	fun updated_whenMaxGradeIsNotUsable_hidesGradeChip() {
		assertFalse(
			gradeSectionItem().updated(
				grade = null,
				maxGrade = null
			).showsGradeChip
		)
		assertFalse(
			gradeSectionItem().updated(
				grade = null,
				maxGrade = 0.0
			).showsGradeChip
		)
	}

	@Test
	fun titleText_whenGradeChipVisibilityChanges_switchesBetweenTitles() {
		val gradableItem = gradeSectionItem().updated(
			grade = null,
			maxGrade = 20.0
		)
		val maxGradeOnlyItem = gradeSectionItem().updated(
			grade = null,
			maxGrade = null
		)

		assertEquals(OVERDUE_TITLE, gradableItem.titleText)
		assertEquals(MAX_GRADE_TITLE, maxGradeOnlyItem.titleText)
	}

	private fun gradeSectionItem() = EvaluationGradeSectionItem(
		maxGradeTitleText = MAX_GRADE_TITLE,
		overdueTitleText = OVERDUE_TITLE,
		gradeText = "",
		maxGradeText = "",
		showsGradeChip = false
	)
}

private const val MAX_GRADE_TITLE = "Nota máxima"
private const val OVERDUE_TITLE = "Notas"
