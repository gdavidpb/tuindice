package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CardCommonViewsUiTest {
	@Test
	fun when_figuresAreShown_then_eachReadsAsItsFullSentence() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardFiguresView(
				primary = CardFigure("3.8125", "índice del trimestre", "Índice del trimestre: 3.8125"),
				secondary = CardFigure("3.4521", "acumulado", "Índice acumulado: 3.4521")
			)
		}

		onNodeWithContentDescription("Índice del trimestre: 3.8125").assertIsDisplayed()
		onNodeWithContentDescription("Índice acumulado: 3.4521").assertIsDisplayed()
	}

	@Test
	fun when_aRowWithChipAndALinkAreShown_then_theyReadAndTheLinkReportsTheTap() = runTuIndiceUiTest {
		var taps = 0
		setTuIndiceTestContent {
			Column {
				CardLabelView(text = "Sep–Dic 2026")
				CardRowView(code = "FS2211", name = "Física III") {
					CardChipView(text = "En curso", tone = CardChipTone.Current)
				}
				CardRowView(code = "CI4325", name = "Arquitectura", showsDivider = true) { CardValueView(text = "4") }
				CardProgressView(fraction = 0.47f, color = Color.Gray)
				CardLinkView(text = "Ver en Informe", onClick = { taps++ })
			}
		}

		onNodeWithText("SEP–DIC 2026").assertIsDisplayed()
		onNodeWithText("En curso").assertIsDisplayed()
		onNodeWithText("4").assertIsDisplayed()
		onNodeWithText("Ver en Informe").performClick()
		assertEquals(1, taps)
	}
}
