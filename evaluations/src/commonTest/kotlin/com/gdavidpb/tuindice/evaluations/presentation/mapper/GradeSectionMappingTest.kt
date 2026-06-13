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
			isOverdue = false,
			grade = 18.5,
			maxGrade = 20.0
		)

		assertEquals("18.50", updated.gradeText)
		assertEquals("20.00", updated.maxGradeText)
	}

	@Test
	fun updated_whenGradesAreMissing_fallsBackToZero() {
		val updated = gradeSectionItem().updated(
			isOverdue = false,
			grade = null,
			maxGrade = null
		)

		assertEquals("0.00", updated.gradeText)
		assertEquals("0.00", updated.maxGradeText)
	}

	@Test
	fun updated_whenEvaluationIsOverdueWithPositiveMaxGrade_showsGradeChip() {
		val updated = gradeSectionItem().updated(
			isOverdue = true,
			grade = null,
			maxGrade = 20.0
		)

		assertTrue(updated.showsGradeChip)
	}

	@Test
	fun updated_whenEvaluationIsOverdueWithoutUsableMaxGrade_hidesGradeChip() {
		assertFalse(
			gradeSectionItem().updated(
				isOverdue = true,
				grade = null,
				maxGrade = null
			).showsGradeChip
		)
		assertFalse(
			gradeSectionItem().updated(
				isOverdue = true,
				grade = null,
				maxGrade = 0.0
			).showsGradeChip
		)
	}

	@Test
	fun updated_whenEvaluationIsNotOverdue_hidesGradeChip() {
		assertFalse(
			gradeSectionItem().updated(
				isOverdue = false,
				grade = 10.0,
				maxGrade = 20.0
			).showsGradeChip
		)
	}

	@Test
	fun titleText_whenGradeChipVisibilityChanges_switchesBetweenTitles() {
		val overdueItem = gradeSectionItem().updated(
			isOverdue = true,
			grade = null,
			maxGrade = 20.0
		)
		val regularItem = gradeSectionItem().updated(
			isOverdue = false,
			grade = null,
			maxGrade = 20.0
		)

		assertEquals(OVERDUE_TITLE, overdueItem.titleText)
		assertEquals(MAX_GRADE_TITLE, regularItem.titleText)
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
