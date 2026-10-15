package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.testing.summaryTermWeekItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryTermWeekViewUiTest {
	@Test
	fun when_theWeekIsInsideTheTerm_then_itShowsWhatIsLeftOfIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermWeekView(week = summaryTermWeekItem(week = 3))
		}

		onNodeWithTag(SummaryUiTags.TermWeekRemaining).assertTextEquals("faltan 9 semanas")
	}

	@Test
	fun when_theWeekIsTheLastOne_then_itSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermWeekView(week = summaryTermWeekItem(week = 12))
		}

		onNodeWithTag(SummaryUiTags.TermWeekRemaining).assertTextEquals("última semana")
	}
}
