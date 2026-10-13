package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.testing.summaryPointsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

	@Test
	fun when_termHasNoWeek_then_theCardKeepsTheSameSpaceAboveItsFirstRowAndBelowItsLast() = runTuIndiceUiTest {
		val pointsOnly = term.copy(
			week = null,
			subjects = listOf(
				summaryPointsItem(subjectCode = "CI5311", earned = "41"),
				summaryPointsItem(subjectCode = "CI5437", earned = "18", evaluated = "25")
			)
		)
		lateinit var density: Density

		setTuIndiceTestContent {
			density = LocalDensity.current
			SummaryTermView(term = pointsOnly, actions = SummaryHubActions())
		}

		val spaces = cardSpaces(density = density, firstText = "41 de 60 puntos", lastText = "18 de 25 puntos")

		assertSameSpace(spaces, density)
	}

	@Test
	fun when_termHasNoPointsYet_then_theCardKeepsTheSameSpaceAboveItsWeekAndBelowItsTrack() = runTuIndiceUiTest {
		lateinit var density: Density

		setTuIndiceTestContent {
			density = LocalDensity.current
			SummaryTermView(term = term.copy(subjects = emptyList()), actions = SummaryHubActions())
		}

		val track = onNodeWithTag(SummaryUiTags.TermWeekTrack).fetchSemanticsNode().boundsInRoot
		val week = onNodeWithText("Semana 3 de 12").fetchSemanticsNode().boundsInRoot
		val spaces = Pair(week.top - cardTop(density), sectionBottom() - track.bottom)

		assertSameSpace(spaces, density)
	}

	// The card starts one spacing under the header, which is as tall as its link.
	private fun ComposeUiTest.cardTop(density: Density): Float {
		val link = onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).fetchSemanticsNode()

		return link.boundsInRoot.bottom + with(density) { TuIndiceSpacing.Medium.toPx() }
	}

	private fun ComposeUiTest.sectionBottom(): Float {
		return onNodeWithTag(SummaryUiTags.TermSection).fetchSemanticsNode().boundsInRoot.bottom
	}

	private fun ComposeUiTest.cardSpaces(
		density: Density,
		firstText: String,
		lastText: String
	): Pair<Float, Float> {
		val first = onNodeWithText(firstText).fetchSemanticsNode().boundsInRoot
		val last = onNodeWithText(lastText).fetchSemanticsNode().boundsInRoot

		return Pair(first.top - cardTop(density), sectionBottom() - last.bottom)
	}

	private fun assertSameSpace(spaces: Pair<Float, Float>, density: Density) {
		val (above, below) = spaces

		assertTrue(
			kotlin.math.abs(above - below) <= with(density) { 1.dp.toPx() },
			"the card leaves $above px above its content and $below px below it"
		)
	}
}
