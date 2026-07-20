package com.gdavidpb.tuindice.evaluations.presentation.mapper

import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_add_evaluation_grades
import tuindice.evaluations.generated.resources.label_add_evaluation_max_grade
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Resolves titles through getString, so it runs on the iOS host only (androidHostTestExcludedPatterns).
class GradeSectionMappingUiTest {
	@Test
	fun when_gradeSectionItemIsBuilt_then_resolvesTitlesFromResources() = runTest {
		val item = getEvaluationGradeSectionItem(
			grade = 18.5,
			maxGrade = 20.0
		)

		assertEquals(getString(Res.string.label_add_evaluation_max_grade), item.maxGradeTitleText)
		assertEquals(getString(Res.string.label_add_evaluation_grades), item.overdueTitleText)
		assertEquals("18.50", item.gradeText)
		assertEquals("20.00", item.maxGradeText)
	}

	@Test
	fun when_maxGradeIsUsable_then_showsGradeChipAndGradesTitle() = runTest {
		val item = getEvaluationGradeSectionItem(
			grade = null,
			maxGrade = 20.0
		)

		assertTrue(item.showsGradeChip)
		assertEquals(getString(Res.string.label_add_evaluation_grades), item.titleText)
		assertEquals("0.00", item.gradeText)
	}

	@Test
	fun when_maxGradeIsNotUsable_then_hidesGradeChipAndUsesMaxGradeTitle() = runTest {
		val item = getEvaluationGradeSectionItem(
			grade = null,
			maxGrade = null
		)

		assertFalse(item.showsGradeChip)
		assertEquals(getString(Res.string.label_add_evaluation_max_grade), item.titleText)
	}
}
