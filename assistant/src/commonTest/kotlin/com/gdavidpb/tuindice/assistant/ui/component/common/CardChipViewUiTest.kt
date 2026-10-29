package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CardChipViewUiTest {
	@Test
	fun when_aChipHasAnyTone_then_itShowsItsWordWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				CardChipTone.entries.forEach { tone -> CardChipView(text = "Chip ${tone.name}", tone = tone) }
			}
		}
		waitForIdle()

		CardChipTone.entries.forEach { tone -> onNodeWithText("Chip ${tone.name}").assertIsDisplayed() }
	}

	@Test
	fun when_aChipIsNeutral_then_itShowsItsWordLikeAnyOther() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardChipView(text = "Calificada")
		}
		waitForIdle()

		onNodeWithText("Calificada").assertIsDisplayed()
	}
}
