package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.testing.samplePensumModalityItems
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumModalityOptionRowUiTest {
	@Test
	fun when_rowIsSelected_then_exposesASelectedRadioOptionWithTheModalityName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumModalityOptionRow(
				modifier = Modifier.testTag(ROW_TAG),
				modality = samplePensumModalityItems().last(),
				isSelected = true,
				onClick = {}
			)
		}

		onNodeWithTag(ROW_TAG)
			.assertTextEquals("Pasantía Larga")
			.assertIsSelected()
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
	}

	@Test
	fun when_rowIsTapped_then_reportsTheClickAndFollowsTheNewSelectionState() = runTuIndiceUiTest {
		val isSelectedState = mutableStateOf(false)
		var clickCount = 0

		setTuIndiceTestContent {
			PensumModalityOptionRow(
				modifier = Modifier.testTag(ROW_TAG),
				modality = samplePensumModalityItems().first(),
				isSelected = isSelectedState.value,
				onClick = {
					clickCount += 1
					isSelectedState.value = true
				}
			)
		}

		onNodeWithTag(ROW_TAG)
			.assertTextEquals("Proyecto de Grado")
			.assertIsNotSelected()
			.performClick()
		waitForIdle()

		assertEquals(1, clickCount)
		onNodeWithTag(ROW_TAG).assertIsSelected()
	}
}

private const val ROW_TAG = "pensum_modality_option_row"
