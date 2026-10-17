package com.gdavidpb.tuindice.assistant.ui.model

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_term_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_term_index_description
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ChatCardFigureUiTest {
	@Test
	fun when_anIndexIsAFigure_then_itReadsAsItsSentenceWithTheComma() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardFiguresView(
				primary = 3.8125.toCardIndexFigure(
					caption = Res.string.assistant_card_a_term_index_caption,
					description = Res.string.assistant_card_a_term_index_description
				)
			)
		}

		onNodeWithText("3,8125", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("índice del trimestre", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithContentDescription("Índice del trimestre: 3,8125").assertIsDisplayed()
	}

	@Test
	fun when_anIndexHasNoDecimals_then_itsFourDecimalsAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardFiguresView(
				primary = 16.0.toCardIndexFigure(
					caption = Res.string.assistant_card_a_term_index_caption,
					description = Res.string.assistant_card_a_term_index_description
				)
			)
		}

		onNodeWithText("16,0000", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithContentDescription("Índice del trimestre: 16,0000").assertIsDisplayed()
	}
}
