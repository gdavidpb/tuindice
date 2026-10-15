package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsFiguresCardUiTest {
	@Test
	fun when_termsAreOver_then_theGradeAndThePhraseAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardHost(onClick = {})
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsFigureText, useUnmergedTree = true)
		onNodeWithText("44 aprobadas · mejor trimestre Ene - Mar 2024", useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_theCardIsTapped_then_ItOpensTheStatistics() = runTuIndiceUiTest {
		var opened = false

		setTuIndiceTestContent {
			CardHost(onClick = { opened = true })
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsCard).performClick()

		assertTrue(opened)
	}

	@Composable
	private fun CardHost(onClick: () -> Unit) {
		Column {
			SummaryStatisticsFiguresCard(
				item = SummaryStatisticsSectionItem.Figures(
					grade = 4.125f,
					line = listOf(4.1f, 4.125f),
					phrase = words("44 aprobadas · mejor trimestre Ene - Mar 2024")
				),
				onClick = onClick
			)
		}
	}
}
