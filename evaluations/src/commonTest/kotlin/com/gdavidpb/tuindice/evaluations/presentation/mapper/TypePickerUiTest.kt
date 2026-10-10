package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

// The two composables of TypePicker.kt: the labels of every type read from the resources, and
// the picker items built from them. Both are rendered the way a picker would, one node per type.
@OptIn(ExperimentalTestApi::class)
class TypePickerUiTest {
	@Test
	fun when_typeLabelsAreRemembered_then_everyTypeReadsItsOwnSpanishLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val typeLabels = rememberEvaluationTypeLabels()

			Column {
				EvaluationType.entries.forEach { type ->
					Text(
						modifier = Modifier.testTag(typeTag(type)),
						text = typeLabels.getValue(type)
					)
				}
			}
		}

		waitForLabel("Trabajo escrito")

		onNodeWithTag(typeTag(EvaluationType.TEST)).assertTextEquals("Parcial")
		onNodeWithTag(typeTag(EvaluationType.ESSAY)).assertTextEquals("Ensayo")
		onNodeWithTag(typeTag(EvaluationType.QUIZ)).assertTextEquals("Quiz")
		onNodeWithTag(typeTag(EvaluationType.WRITTEN_WORK)).assertTextEquals("Trabajo escrito")
		onNodeWithTag(typeTag(EvaluationType.OTHER)).assertTextEquals("Otra")
	}

	@Test
	fun when_noTypeIsSelected_then_everyItemIsVisibleWithItsLabel_andNoneIsSelected() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TypePickerItems(selectedType = null)
		}

		waitForLabel("Trabajo escrito")

		EvaluationType.entries.forEach { type ->
			onNodeWithTag(typeTag(type)).assert(isSelected(false))
		}
		onNodeWithTag(typeTag(EvaluationType.TEST)).assertTextEquals("Parcial")
		onNodeWithTag(typeTag(EvaluationType.OTHER)).assertTextEquals("Otra")
	}

	@Test
	fun when_theSelectedTypeChanges_then_itemsCollapseToThatType_andBackToAll() = runTuIndiceUiTest {
		val selectedTypeState = mutableStateOf<EvaluationType?>(EvaluationType.QUIZ)

		setTuIndiceTestContent {
			TypePickerItems(selectedType = selectedTypeState.value)
		}

		waitForLabel("Quiz")

		onNodeWithTag(typeTag(EvaluationType.QUIZ))
			.assertTextEquals("Quiz")
			.assert(isSelected(true))
		assertNodeHidden(typeTag(EvaluationType.TEST))
		assertNodeHidden(typeTag(EvaluationType.OTHER))

		runOnIdle {
			selectedTypeState.value = EvaluationType.TEST
		}

		onNodeWithTag(typeTag(EvaluationType.TEST))
			.assertTextEquals("Parcial")
			.assert(isSelected(true))
		assertNodeHidden(typeTag(EvaluationType.QUIZ))

		runOnIdle {
			selectedTypeState.value = null
		}

		onNodeWithTag(typeTag(EvaluationType.TEST)).assert(isSelected(false))
		onNodeWithTag(typeTag(EvaluationType.QUIZ)).assert(isSelected(false))
		onNodeWithTag(typeTag(EvaluationType.OTHER)).assertTextEquals("Otra")
	}

	// Resources resolve asynchronously off Android: wait for the text before reading the nodes.
	private fun ComposeUiTest.waitForLabel(label: String) {
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun isSelected(selected: Boolean) =
		SemanticsMatcher.expectValue(SemanticsProperties.Selected, selected)
}

@Composable
private fun TypePickerItems(selectedType: EvaluationType?) {
	val items = rememberEvaluationTypePickerItemList(selectedType = selectedType)

	Column {
		items
			.filter { item -> item.isVisible }
			.forEach { item ->
				Text(
					modifier = Modifier
						.testTag(typeTag(item.type))
						.semantics { selected = item.isSelected },
					text = item.labelText
				)
			}
	}
}

private fun typeTag(type: EvaluationType) = "type_picker_item_${type.name}"

private const val RESOURCE_TIMEOUT_MILLIS = 5_000L
