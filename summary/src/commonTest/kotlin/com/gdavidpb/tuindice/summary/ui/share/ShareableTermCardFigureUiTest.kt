package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardFigureUiTest {
	@Test
	fun when_itIsShown_then_itShowsItsValueAndItsLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCardFigure(figure = SummaryStatisticsFigureItem(value = "7", label = UiText.Raw("materias")))
		}

		onNodeWithText("7").assertIsDisplayed()
		onNodeWithText("materias").assertIsDisplayed()
	}

	@Test
	fun when_itIsShownWithAnotherValue_then_thatValueIsTheOneShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCardFigure(figure = SummaryStatisticsFigureItem(value = "12", label = UiText.Raw("créditos")))
		}

		onNodeWithText("12").assertIsDisplayed()
		onNodeWithText("créditos").assertIsDisplayed()
	}
}
