package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailHighlightKpiCardUiTest {
	@Test
	fun when_rendered_then_displaysTitleValueAndSupportingText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailHighlightKpiCard(
				title = "Score de dificultad",
				value = "41 / 100",
				supporting = "Media"
			)
		}

		onNodeWithText("Score de dificultad").assertIsDisplayed()
		onNodeWithText("41 / 100").assertIsDisplayed()
		onNodeWithText("Media").assertIsDisplayed()
	}

	@Test
	fun when_modifierProvided_then_cardStacksTitleValueAndSupportingInOrder() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailHighlightKpiCard(
				modifier = Modifier.testTag(CARD_TAG),
				title = "Score de dificultad",
				value = "88 / 100",
				supporting = "Muy alta"
			)
		}

		val texts = onNodeWithTag(CARD_TAG).onChildren()

		texts.assertCountEquals(3)
		texts[0].assertTextEquals("Score de dificultad")
		texts[1].assertTextEquals("88 / 100")
		texts[2].assertTextEquals("Muy alta")
	}

	private companion object {
		const val CARD_TAG = "highlight_kpi_card"
	}
}
