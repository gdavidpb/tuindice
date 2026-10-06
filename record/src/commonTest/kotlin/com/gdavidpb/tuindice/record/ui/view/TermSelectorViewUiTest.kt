package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.testing.termItem
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TermSelectorViewUiTest {
	@Test
	fun when_aTermIsSelected_then_itsChipIsMarkedSelected_andItsNameIsTheBoldOne() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(
					termItem(termId = "2026-APR_JUL", shortNameText = "Abr - Jul 2026"),
					termItem(termId = "2026-SEP_DEC", shortNameText = "Sep - Dic 2026")
				),
				selectedTermId = "2026-APR_JUL",
				onTermSelected = {}
			)
		}

		assertNodeVisible(RecordUiTags.TermSelectorRow)
		onNodeWithTag(RecordUiTags.termChip("2026-APR_JUL"))
			.assertTextEquals("Abr - Jul 2026")
			.assertIsSelected()
		onNodeWithTag(RecordUiTags.termChip("2026-SEP_DEC"))
			.assertTextEquals("Sep - Dic 2026")
			.assertIsNotSelected()
		assertEquals(FontWeight.Bold, weightOf("Abr - Jul 2026"))
		assertEquals(FontWeight.Medium, weightOf("Sep - Dic 2026"))
	}

	@Test
	fun when_oneOfTheTermsIsTheCurrentOne_then_onlyItsChipCarriesTheCurrentMark() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(
					termItem(termId = "historical", shortNameText = "Abr - Jul 2026"),
					termItem(termId = "current", shortNameText = "Sep - Dic 2026", kind = TermItemKind.CURRENT)
				),
				selectedTermId = "current",
				onTermSelected = {}
			)
		}

		// The selected chip is the one centred on screen; its neighbour only peeks in from the side.
		// The mark is a dot inside the merged chip, so it is read from the unmerged tree.
		assertNodeVisible(RecordUiTags.termCurrentChip("current"), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.termCurrentChip("historical"), useUnmergedTree = true)
	}

	@Test
	fun when_theSelectedTermIsTappedAgain_then_itIsStillReported() = runTuIndiceUiTest {
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(termItem(termId = "only-term")),
				selectedTermId = "only-term",
				onTermSelected = { termId -> reported += termId }
			)
		}

		onNodeWithTag(RecordUiTags.termChip("only-term"))
			.assertIsDisplayed()
			.performClick()

		// Whether choosing the term already chosen means anything is for the caller to decide.
		assertEquals(listOf("only-term"), reported)
	}

	@Test
	fun when_noTermMatchesTheSelection_then_noChipIsSelected() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(termItem(termId = "only-term")),
				selectedTermId = null,
				onTermSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.termChip("only-term"))
			.assertIsDisplayed()
			.assertIsNotSelected()
	}

	private fun ComposeUiTest.weightOf(name: String): FontWeight? =
		onNodeWithText(name, useUnmergedTree = true).textLayout().layoutInput.style.fontWeight
}
