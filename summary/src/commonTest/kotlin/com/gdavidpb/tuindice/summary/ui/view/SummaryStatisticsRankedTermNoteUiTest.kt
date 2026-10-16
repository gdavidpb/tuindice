package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsRankedTermNoteUiTest {
	@Test
	fun when_aRankedTermIsGiven_then_ItsWordsAreWrittenUnderItsTag() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			NoteHost(text = "Mejor: Ene - Mar 2024 · 4.5714", tag = SummaryUiTags.StatisticsBestTerm)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsBestTerm)
		onNodeWithText("Mejor: Ene - Mar 2024 · 4.5714").assertIsDisplayed()
	}

	@Test
	fun when_theWorstTermIsGiven_then_ItIsTaggedAsTheWorst() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			NoteHost(text = "Peor: Sep - Dic 2023 · 4.2000", tag = SummaryUiTags.StatisticsWorstTerm)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsWorstTerm)
		onNodeWithText("Peor: Sep - Dic 2023 · 4.2000").assertIsDisplayed()
	}

	@Composable
	private fun NoteHost(text: String, tag: String) {
		Column {
			SummaryStatisticsRankedTermNote(text = text, color = Color.Green, tag = tag)
		}
	}
}
