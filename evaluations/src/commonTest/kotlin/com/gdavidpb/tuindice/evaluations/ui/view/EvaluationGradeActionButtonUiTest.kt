package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

// The button colours are only drawn: nothing here reads them.
@OptIn(ExperimentalTestApi::class)
class EvaluationGradeActionButtonUiTest {
	@Test
	fun when_rendered_then_readsTheGradeText_asAnEnabledButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationGradeActionButton(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationGradeActionButton),
				text = "10 / 35",
				colors = ButtonDefaults.filledTonalButtonColors(),
				onClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationGradeActionButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationGradeActionButton)
			.assertTextEquals("10 / 35")
			.assertHasClickAction()
			.assertIsEnabled()
	}

	@Test
	fun when_tapped_then_invokesTheClickCallback() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			EvaluationGradeActionButton(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationGradeActionButton),
				text = "-- / 35",
				colors = ButtonDefaults.filledTonalButtonColors(),
				onClick = { clicks++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationGradeActionButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationGradeActionButton).performClick()

		assertEquals(1, clicks)
	}

	@Test
	fun when_theGradeIsAssigned_then_readsTheNewGradeText() = runTuIndiceUiTest {
		val textState = mutableStateOf("-- / 35")

		setTuIndiceTestContent {
			EvaluationGradeActionButton(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationGradeActionButton),
				text = textState.value,
				colors = ButtonDefaults.filledTonalButtonColors(),
				onClick = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeActionButton).assertTextEquals("-- / 35")

		runOnIdle {
			textState.value = "32 / 35"
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeActionButton).assertTextEquals("32 / 35")
	}
}
