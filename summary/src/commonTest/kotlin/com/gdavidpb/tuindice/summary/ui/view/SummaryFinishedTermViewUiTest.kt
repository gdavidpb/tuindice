package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFinishedTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryFinishedTermViewUiTest {
	private val finished = SummaryFinishedTermItem(
		nameText = UiText.Raw("Abr - Jul 2026"),
		stateText = UiText.Raw("terminado"),
		indexLabel = UiText.Raw("Índice del trimestre"),
		indexText = "14.5000",
		creditsLabel = UiText.Raw("Créditos aprobados"),
		creditsText = UiText.Raw("16 UC"),
		termKey = "2026-APR_JUL",
		isShareable = true
	)

	@Test
	fun when_rendered_then_itShowsItsNameIndexAndApprovedCredits() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFinishedTermView(item = finished, onClick = {}, onShareClick = {})
		}

		onNodeWithTag(SummaryUiTags.TermFinishedName, useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("Índice del trimestre").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.TermFinishedIndex, useUnmergedTree = true).assertTextEquals("14.5000")
		onNodeWithText("Créditos aprobados").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.TermFinishedCredits, useUnmergedTree = true).assertTextEquals("16 UC")
	}

	@Test
	fun when_tapped_then_itAsksForRecordOnce() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummaryFinishedTermView(item = finished, onClick = { taps++ }, onShareClick = {})
		}

		onNodeWithTag(SummaryUiTags.TermFinishedCard).assertHasClickAction()
		onNodeWithTag(SummaryUiTags.TermFinishedCard).performClick()
		assertEquals(1, taps)
	}

	// The button is a sibling of the body, not a part of it: inside the tappable body it would be merged into the body and
	// could not be found on its own.
	@Test
	fun when_shareTapped_then_itAsksForThatTermOnce_andNotForRecord() = runTuIndiceUiTest {
		var recordTaps = 0
		val shared = mutableListOf<String>()

		setTuIndiceTestContent {
			SummaryFinishedTermView(
				item = finished,
				onClick = { recordTaps++ },
				onShareClick = { termKey -> shared += termKey }
			)
		}

		onNodeWithTag(SummaryUiTags.TermFinishedShare).performClick()

		assertEquals(listOf("2026-APR_JUL"), shared)
		assertEquals(0, recordTaps)
	}

	@Test
	fun when_bodyTapped_then_itAsksForRecord_andNotForShare() = runTuIndiceUiTest {
		var recordTaps = 0
		val shared = mutableListOf<String>()

		setTuIndiceTestContent {
			SummaryFinishedTermView(
				item = finished,
				onClick = { recordTaps++ },
				onShareClick = { termKey -> shared += termKey }
			)
		}

		onNodeWithTag(SummaryUiTags.TermFinishedCard).performClick()

		assertEquals(1, recordTaps)
		assertEquals(emptyList<String>(), shared)
	}

	@Test
	fun when_termCannotBeShared_then_thereIsNoShareButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFinishedTermView(item = finished.copy(isShareable = false), onClick = {}, onShareClick = {})
		}

		onNodeWithTag(SummaryUiTags.TermFinishedShare, useUnmergedTree = true).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.TermFinishedCard).assertIsDisplayed()
	}
}
