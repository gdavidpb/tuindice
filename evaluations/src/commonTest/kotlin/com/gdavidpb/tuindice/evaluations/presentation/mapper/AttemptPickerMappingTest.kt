package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AttemptPickerMappingTest {
	@Test
	fun toEvaluationAttemptPickerItems_whenNoAttemptIsSelected_showsAllAttemptsUnselected() {
		val items = listOf(
			DEFAULT_EVALUATION_SUBJECT,
			SECOND_EVALUATION_SUBJECT
		).toEvaluationAttemptPickerItems(selectedAttempt = null)

		assertEquals(
			listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
			items.map { item -> item.attempt }
		)
		assertEquals(
			listOf(DEFAULT_EVALUATION_SUBJECT.code, SECOND_EVALUATION_SUBJECT.code),
			items.map { item -> item.labelText }
		)
		assertTrue(items.all { item -> item.isVisible })
		assertTrue(items.none { item -> item.isSelected })
	}

	@Test
	fun toEvaluationAttemptPickerItems_whenMapped_derivesColorsFromCourseCode() {
		val expectedColors = CourseCodeColorGenerator.fromCode(DEFAULT_EVALUATION_SUBJECT.code)

		val item = listOf(DEFAULT_EVALUATION_SUBJECT)
			.toEvaluationAttemptPickerItems(selectedAttempt = null)
			.single()

		assertEquals(expectedColors.containerColor, item.containerColor)
		assertEquals(expectedColors.color, item.contentColor)
		assertEquals(
			expectedColors.containerColor.copy(alpha = TuIndiceAlpha.Muted),
			item.disabledContainerColor
		)
		assertEquals(
			expectedColors.color.copy(alpha = 0.38f),
			item.disabledContentColor
		)
	}

	@Test
	fun toEvaluationAttemptPickerItems_whenAttemptIsNotNumeric_excludesIt() {
		val qualitativeAttempt = SECOND_EVALUATION_SUBJECT.copy(
			gradingMode = GradingMode.QUALITATIVE_PASS_FAIL
		)

		val items = listOf(
			DEFAULT_EVALUATION_SUBJECT,
			qualitativeAttempt
		).toEvaluationAttemptPickerItems(selectedAttempt = null)

		assertEquals(listOf(DEFAULT_EVALUATION_SUBJECT), items.map { item -> item.attempt })
	}

	@Test
	fun toEvaluationAttemptPickerItems_whenAttemptIsSelected_hidesEveryOtherAttempt() {
		val items = listOf(
			DEFAULT_EVALUATION_SUBJECT,
			SECOND_EVALUATION_SUBJECT
		).toEvaluationAttemptPickerItems(selectedAttempt = DEFAULT_EVALUATION_SUBJECT)

		val selectedItem = items.first { item -> item.attempt == DEFAULT_EVALUATION_SUBJECT }
		val unselectedItem = items.first { item -> item.attempt == SECOND_EVALUATION_SUBJECT }

		assertTrue(selectedItem.isSelected)
		assertTrue(selectedItem.isVisible)
		assertFalse(unselectedItem.isSelected)
		assertFalse(unselectedItem.isVisible)
	}

	@Test
	fun toEvaluationAttemptPickerItems_whenListIsEmpty_returnsEmptyList() {
		assertTrue(
			emptyList<EditableAttemptDescriptor>()
				.toEvaluationAttemptPickerItems(selectedAttempt = null)
				.isEmpty()
		)
	}

	@Test
	fun withSelectedAttempt_whenSelectionChanges_movesSelectionAndVisibility() {
		val items = listOf(
			DEFAULT_EVALUATION_SUBJECT,
			SECOND_EVALUATION_SUBJECT
		).toEvaluationAttemptPickerItems(selectedAttempt = DEFAULT_EVALUATION_SUBJECT)

		val reselected = items.withSelectedAttempt(selectedAttempt = SECOND_EVALUATION_SUBJECT)

		assertFalse(reselected.first { item -> item.attempt == DEFAULT_EVALUATION_SUBJECT }.isVisible)
		assertTrue(reselected.first { item -> item.attempt == SECOND_EVALUATION_SUBJECT }.isSelected)
	}

	@Test
	fun withSelectedAttempt_whenSelectionIsCleared_restoresVisibilityForAllAttempts() {
		val items = listOf(
			DEFAULT_EVALUATION_SUBJECT,
			SECOND_EVALUATION_SUBJECT
		).toEvaluationAttemptPickerItems(selectedAttempt = DEFAULT_EVALUATION_SUBJECT)

		val cleared = items.withSelectedAttempt(selectedAttempt = null)

		assertTrue(cleared.all { item -> item.isVisible })
		assertTrue(cleared.none { item -> item.isSelected })
	}
}
