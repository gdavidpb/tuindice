package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypePickerMappingTest {
	@Test
	fun toEvaluationTypePickerItemList_whenNoTypeIsSelected_mapsEveryTypeVisibleAndUnselected() {
		val items = EvaluationType.entries.toEvaluationTypePickerItemList(
			selectedType = null,
			typeLabels = testTypeLabels()
		)

		assertEquals(EvaluationType.entries.toList(), items.map { item -> item.type })
		assertTrue(items.all { item -> item.isVisible })
		assertTrue(items.none { item -> item.isSelected })
	}

	@Test
	fun toEvaluationTypePickerItemList_whenMapped_usesProvidedLabelsAndTypeIcons() {
		val typeLabels = testTypeLabels()

		val items = EvaluationType.entries.toEvaluationTypePickerItemList(
			selectedType = null,
			typeLabels = typeLabels
		)

		items.forEach { item ->
			assertEquals(typeLabels.getValue(item.type), item.labelText)
			assertEquals(item.type.asIcon(), item.icon)
		}
	}

	@Test
	fun toEvaluationTypePickerItemList_whenTypeIsSelected_hidesEveryOtherType() {
		val items = EvaluationType.entries.toEvaluationTypePickerItemList(
			selectedType = EvaluationType.QUIZ,
			typeLabels = testTypeLabels()
		)

		val selectedItem = items.first { item -> item.type == EvaluationType.QUIZ }

		assertTrue(selectedItem.isSelected)
		assertTrue(selectedItem.isVisible)
		assertTrue(
			items
				.filterNot { item -> item.type == EvaluationType.QUIZ }
				.none { item -> item.isSelected || item.isVisible }
		)
	}

	@Test
	fun withSelectedType_whenSelectionChanges_movesSelectionAndVisibility() {
		val items = EvaluationType.entries.toEvaluationTypePickerItemList(
			selectedType = EvaluationType.QUIZ,
			typeLabels = testTypeLabels()
		)

		val reselected = items.withSelectedType(selectedType = EvaluationType.TEST)

		assertFalse(reselected.first { item -> item.type == EvaluationType.QUIZ }.isVisible)
		assertTrue(reselected.first { item -> item.type == EvaluationType.TEST }.isSelected)
	}

	@Test
	fun withSelectedType_whenSelectionIsCleared_restoresVisibilityForAllTypes() {
		val items = EvaluationType.entries.toEvaluationTypePickerItemList(
			selectedType = EvaluationType.QUIZ,
			typeLabels = testTypeLabels()
		)

		val cleared = items.withSelectedType(selectedType = null)

		assertTrue(cleared.all { item -> item.isVisible })
		assertTrue(cleared.none { item -> item.isSelected })
	}

	private fun testTypeLabels(): Map<EvaluationType, String> {
		return EvaluationType.entries.associateWith { type -> "label-${type.name}" }
	}
}
