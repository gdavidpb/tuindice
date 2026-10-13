package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryTermViewUiTest {
	private val term = summaryHubItem().term!!

	@Test
	fun when_termHasAWeek_then_itShowsTheWeekTheTrackAndEachSubjectsPoints() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term, actions = SummaryHubActions())
		}

		assertNodeVisible(SummaryUiTags.TermSection)
		onNodeWithText("Trimestre").assertIsDisplayed()
		onNodeWithText("Sep - Dic 2026").assertIsDisplayed()
		onNodeWithText("Semana 3 de 12").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.TermWeekTrack).assertContentDescriptionEquals("Semana 3 de 12")
		onNodeWithText("41 de 60 puntos").assertIsDisplayed()
		onNodeWithText("18 de 25 puntos").assertIsDisplayed()
		onNodeWithText("Sin notas aún").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.termPointsRow("EG1114"))
	}

	@Test
	fun when_termHasNoWeek_then_onlyThePointsRemain() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term.copy(week = null), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.TermWeekText).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.TermWeekTrack).assertDoesNotExist()
		onNodeWithText("41 de 60 puntos").assertIsDisplayed()
	}

	@Test
	fun when_termHasNoPointsYet_then_itSaysNothingOfTheSubjects() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term.copy(subjects = emptyList()), actions = SummaryHubActions())
		}

		onNodeWithText("Semana 3 de 12").assertIsDisplayed()
		onNodeWithText("Sin notas aún").assertDoesNotExist()
	}

	@Test
	fun when_theLinkIsTapped_then_itAsksForTheRecord() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryTermView(term = term, actions = SummaryHubActions(onTargetClick = { target -> targets += target }))
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Record), targets)
	}
}
