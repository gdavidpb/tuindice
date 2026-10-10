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
class SubjectDetailStandardKpiCardUiTest {
	@Test
	fun when_rendered_then_displaysTitleAndValue() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailStandardKpiCard(
				title = "Primer intento",
				value = "55%"
			)
		}

		onNodeWithText("Primer intento").assertIsDisplayed()
		onNodeWithText("55%").assertIsDisplayed()
	}

	@Test
	fun when_modifierProvided_then_cardStacksTitleAboveValue() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailStandardKpiCard(
				modifier = Modifier.testTag(CARD_TAG),
				title = "Retiro",
				value = "--"
			)
		}

		val texts = onNodeWithTag(CARD_TAG).onChildren()

		texts.assertCountEquals(2)
		texts[0].assertTextEquals("Retiro")
		texts[1].assertTextEquals("--")
	}

	private companion object {
		const val CARD_TAG = "standard_kpi_card"
	}
}
