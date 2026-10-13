package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

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
