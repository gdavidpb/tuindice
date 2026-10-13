package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryFigureTextUiTest {
	@Test
	fun when_thereIsAFigure_then_figureAndRestAreOneText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFigureText(figure = "41", rest = "de 60 puntos")
		}

		onNodeWithText("41 de 60 puntos").assertIsDisplayed()
	}

	@Test
	fun when_thereIsAFigure_then_itIsPlainAndLargerAndTheRestIsSmallerAndLighter() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFigureText(figure = "41", rest = "de 60 puntos")
		}

		onNodeWithText("41 de 60 puntos").assertIsDisplayed()

		// The one text a screen reader reads: its figure and its rest are the two spans of it.
		val text = onNodeWithText("41 de 60 puntos").fetchSemanticsNode().config[SemanticsProperties.Text].single()
		val figure = text.spanStyles.single { range -> range.start == 0 }
		val rest = text.spanStyles.single { range -> range.end == text.length }

		assertEquals("41", text.text.substring(figure.start, figure.end))
		assertEquals("de 60 puntos", text.text.substring(rest.start, rest.end))
		assertEquals(FontWeight.Normal, figure.item.fontWeight)
		assertTrue(figure.item.fontSize.value > rest.item.fontSize.value)
		assertTrue(rest.item.color.alpha < figure.item.color.alpha)
	}

	@Test
	fun when_thereIsNoFigure_then_onlyTheRestIsSaid() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFigureText(figure = null, rest = "Sin notas aún")
		}

		onNodeWithText("Sin notas aún").assertIsDisplayed()
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_aLongTextWrapsWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummaryFigureText(figure = "156", rest = "de 240 créditos aprobados de tu pensum")
			}
		}

		assertTextIsWhole("156 de 240 créditos aprobados de tu pensum")
	}
}
