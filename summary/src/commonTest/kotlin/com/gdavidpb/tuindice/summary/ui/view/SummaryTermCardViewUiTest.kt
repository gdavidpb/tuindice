package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermWeekItem
import com.gdavidpb.tuindice.summary.testing.summaryTermWeekItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryTermCardViewUiTest {
	@Test
	fun when_theTermHasAWeek_then_itsWeekAndSummaryAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermCardView(term = term(week = summaryTermWeekItem()), actions = SummaryHubActions(onTargetClick = {}))
		}

		onNodeWithTag(SummaryUiTags.TermWeekRemaining).assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.TermSummary).assertTextEquals("Resumen del trimestre")
	}

	@Test
	fun when_theTermHasNoWeek_then_itsSummaryStillShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermCardView(term = term(week = null), actions = SummaryHubActions(onTargetClick = {}))
		}

		onNodeWithTag(SummaryUiTags.TermSummary).assertIsDisplayed()
	}

	private fun term(week: SummaryTermWeekItem?) = SummaryTermItem(
		linkText = UiText.Raw("Registro"),
		week = week,
		summaryText = UiText.Raw("Resumen del trimestre"),
		subjects = emptyList(),
		finished = null,
		card = null
	)
}
