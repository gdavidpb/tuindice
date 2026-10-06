package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

// The container colour, the content colour and the shape are only drawn: nothing here reads them.
@OptIn(ExperimentalTestApi::class)
class EvaluationActionButtonUiTest {
	@Test
	fun when_rendered_then_readsItsText_andShowsTheIconGivenToItsSlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationActionButton(
				modifier = Modifier
					.height(72.dp)
					.testTag(BUTTON_TAG),
				text = "Modificar",
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				shape = RoundedCornerShape(8.dp),
				onClick = {}
			) {
				Box(
					modifier = Modifier
						.size(16.dp)
						.testTag(ICON_TAG)
				)
			}
		}

		assertNodeVisible(BUTTON_TAG)
		onNodeWithTag(BUTTON_TAG).assertTextEquals("Modificar")
		onNodeWithTag(BUTTON_TAG).assertHasClickAction()
		assertNodeVisible(ICON_TAG, useUnmergedTree = true)
	}

	@Test
	fun when_tapped_then_invokesTheClickCallbackOncePerTap() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			EvaluationActionButton(
				modifier = Modifier
					.height(72.dp)
					.testTag(BUTTON_TAG),
				text = "Eliminar",
				containerColor = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.onErrorContainer,
				shape = RoundedCornerShape(8.dp),
				onClick = { clicks++ }
			) {}
		}

		assertNodeVisible(BUTTON_TAG)
		onNodeWithTag(BUTTON_TAG).performClick()
		assertEquals(1, clicks)

		onNodeWithTag(BUTTON_TAG).performClick()
		assertEquals(2, clicks)
	}

	@Test
	fun when_textChanges_then_readsTheNewText() = runTuIndiceUiTest {
		val textState = mutableStateOf("Modificar")

		setTuIndiceTestContent {
			EvaluationActionButton(
				modifier = Modifier
					.height(72.dp)
					.testTag(BUTTON_TAG),
				text = textState.value,
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				shape = RoundedCornerShape(8.dp),
				onClick = {}
			) {}
		}

		onNodeWithTag(BUTTON_TAG).assertTextEquals("Modificar")

		runOnIdle {
			textState.value = "Eliminar"
		}

		onNodeWithTag(BUTTON_TAG).assertTextEquals("Eliminar")
	}

	private companion object {
		const val BUTTON_TAG = "evaluation_action_button_under_test"
		const val ICON_TAG = "evaluation_action_button_icon_under_test"
	}
}
