package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
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
class TermSelectionTermRowViewUiTest {
	@Test
	fun when_theTermIsTheSelectedOne_then_theCheckIsShownAndRead_andItsNameIsBold() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectionTermRowView(
				term = termItem(termId = "current-2026", kind = TermItemKind.CURRENT),
				isSelected = true,
				selectedContentDescription = "Trimestre seleccionado",
				onClick = {}
			)
		}

		assertNodeVisible(RecordUiTags.termSelectionOption("current-2026"))
		// The check lives inside the merged row, so it is read from the unmerged tree.
		onNodeWithTag(RecordUiTags.termSelectionSelectedIcon("current-2026"), useUnmergedTree = true)
			.assertIsDisplayed()
			.assertContentDescriptionEquals("Trimestre seleccionado")
		onNodeWithText("Sep - Dic 2026", useUnmergedTree = true).assertIsDisplayed()
		assertEquals(
			expected = FontWeight.Bold,
			actual = onNodeWithText("Sep - Dic 2026", useUnmergedTree = true).textLayout().layoutInput.style.fontWeight
		)
		// What the term adds up to follows its kind on the same line.
		onNodeWithText("∑x 3.5000", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("⦿ 12", useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_theTermIsNotSelected_then_noCheckIsDrawn_andItsNameIsLighter() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectionTermRowView(
				term = termItem(termId = "historical-2024", shortNameText = "Ene - Mar 2024"),
				isSelected = false,
				selectedContentDescription = "Trimestre seleccionado",
				onClick = {}
			)
		}

		assertNodeVisible(RecordUiTags.termSelectionOption("historical-2024"))
		assertNodeHidden(RecordUiTags.termSelectionSelectedIcon("historical-2024"), useUnmergedTree = true)
		assertEquals(
			expected = FontWeight.SemiBold,
			actual = onNodeWithText("Ene - Mar 2024", useUnmergedTree = true).textLayout().layoutInput.style.fontWeight
		)
	}

	@Test
	fun when_termsOfEachKindAreListed_then_eachRowNamesItsKind() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				TermItemKind.entries.forEach { kind ->
					TermSelectionTermRowView(
						term = termItem(termId = kind.name, kind = kind),
						isSelected = false,
						selectedContentDescription = "Trimestre seleccionado",
						onClick = {}
					)
				}
			}
		}

		mapOf(
			TermItemKind.SYNTHETIC to "Proyección",
			TermItemKind.CURRENT to "Actual",
			TermItemKind.HISTORICAL to "Histórico"
		).forEach { (kind, label) ->
			onNodeWithTag(RecordUiTags.termSelectionKind(kind.name), useUnmergedTree = true)
				.assertIsDisplayed()
				.assert(hasAnyDescendant(hasText(label)))
		}
	}

	@Test
	fun when_theRowIsTapped_then_theTapIsReportedOnce() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			TermSelectionTermRowView(
				term = termItem(termId = "synthetic-2027", kind = TermItemKind.SYNTHETIC),
				isSelected = false,
				selectedContentDescription = "Trimestre seleccionado",
				onClick = { clicks++ }
			)
		}

		onNodeWithTag(RecordUiTags.termSelectionOption("synthetic-2027"))
			.assertHasClickAction()
			.performClick()

		assertEquals(1, clicks)
	}
}
