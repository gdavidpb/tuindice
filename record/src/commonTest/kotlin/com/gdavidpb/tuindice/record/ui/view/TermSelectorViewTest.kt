package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TermSelectorViewTest {
	@Test
	fun when_termTapped_then_invokesSelection() = runTuIndiceUiTest {
		var selectedTermId: String? = null

		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(
					termItem(
						termId = "2027-APR_JUL",
						shortNameText = "Abr - Jul 2027"
					),
					termItem(
						termId = "2027-JUL_AUG",
						shortNameText = "Jul - Ago 2027"
					)
				),
				selectedTermId = "2027-APR_JUL",
				onTermSelected = { selectedTermId = it }
			)
		}

		onNodeWithTag(RecordUiTags.termChip("2027-JUL_AUG"))
			.assertIsDisplayed()
			.performClick()

		assertEquals("2027-JUL_AUG", selectedTermId)
	}

	@Test
	fun when_termHasSyntheticActions_then_selectorDoesNotRenderActionButtons() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(
					termItem(
						termId = "2027-JUL_AUG",
						shortNameText = "Jul - Ago 2027",
						canEdit = true,
						canDelete = true
					)
				),
				selectedTermId = "2027-JUL_AUG",
				onTermSelected = {}
			)
		}

		onAllNodesWithTag(RecordUiTags.EditSyntheticTermButton).assertCountEquals(0)
		onAllNodesWithTag(RecordUiTags.DeleteSyntheticTermButton).assertCountEquals(0)
	}

	private fun termItem(
		termId: String,
		shortNameText: String,
		canEdit: Boolean = false,
		canDelete: Boolean = false
	): TermItem {
		return TermItem(
			termId = termId,
			shortNameText = shortNameText,
			gradeText = AnnotatedString("0.00"),
			gradeDelta = null,
			gradeSumText = AnnotatedString("0.00"),
			gradeSumDelta = null,
			creditsText = AnnotatedString("0"),
			creditsDelta = null,
			isCurrent = false,
			canDelete = canDelete,
			canEdit = canEdit,
			attempts = emptyList()
		)
	}
}
