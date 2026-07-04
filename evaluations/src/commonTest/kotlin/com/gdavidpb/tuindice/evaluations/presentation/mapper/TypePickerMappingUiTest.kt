package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_other
import tuindice.evaluations.generated.resources.evaluation_test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Resolves type labels through getString, so it runs on the iOS host only (androidHostTestExcludedPatterns).
class TypePickerMappingUiTest {
	@Test
	fun getEvaluationTypeLabels_whenResolved_coversEveryTypeWithNonBlankLabels() = runTest {
		val typeLabels = getEvaluationTypeLabels()

		assertEquals(EvaluationType.entries.toSet(), typeLabels.keys)
		assertTrue(typeLabels.values.all { label -> label.isNotBlank() })
	}

	@Test
	fun getEvaluationTypeLabels_whenResolved_matchesExpectedResources() = runTest {
		val typeLabels = getEvaluationTypeLabels()

		assertEquals(
			getString(Res.string.evaluation_test),
			typeLabels.getValue(EvaluationType.TEST)
		)
		assertEquals(
			getString(Res.string.evaluation_other),
			typeLabels.getValue(EvaluationType.OTHER)
		)
	}

	@Test
	fun getEvaluationTypePickerItemList_whenTypeIsSelected_marksOnlyThatTypeVisible() = runTest {
		val items = getEvaluationTypePickerItemList(selectedType = EvaluationType.TEST)

		val selectedItem = items.first { item -> item.type == EvaluationType.TEST }

		assertEquals(EvaluationType.entries.size, items.size)
		assertTrue(selectedItem.isSelected)
		assertTrue(
			items
				.filterNot { item -> item.type == EvaluationType.TEST }
				.none { item -> item.isVisible }
		)
	}
}
