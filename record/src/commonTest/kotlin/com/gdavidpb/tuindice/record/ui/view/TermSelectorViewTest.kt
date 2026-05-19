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
	fun when_selectedTermCanEdit_then_editButtonIsShownAndClickable() = runTuIndiceUiTest {
		var clickedTermId: String? = null
		var selectedTermId: String? = null
		val editableTerm = termItem(
			termId = "2027-JUL_AUG",
			shortNameText = "Jul - Ago 2027",
			canEdit = true
		)

		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(
					termItem(
						termId = "2027-APR_JUL",
						shortNameText = "Abr - Jul 2027",
						canEdit = false
					),
					editableTerm
				),
				selectedTermId = editableTerm.termId,
				onTermSelected = { selectedTermId = it },
				onEditTermClick = { clickedTermId = it }
			)
		}

		onNodeWithTag(RecordUiTags.EditSyntheticTermButton)
			.assertIsDisplayed()
			.performClick()

		assertEquals(editableTerm.termId, clickedTermId)
		assertEquals(null, selectedTermId)
	}

	@Test
	fun when_selectedTermCannotEdit_then_editButtonIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(
					termItem(
						termId = "2027-JUL_AUG",
						shortNameText = "Jul - Ago 2027",
						canEdit = false
					)
				),
				selectedTermId = "2027-JUL_AUG",
				onTermSelected = {},
				onEditTermClick = {}
			)
		}

		onAllNodesWithTag(RecordUiTags.EditSyntheticTermButton).assertCountEquals(0)
	}

	@Test
	fun when_editableTermIsNotSelected_then_editButtonIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectorView(
				terms = listOf(
					termItem(
						termId = "2027-APR_JUL",
						shortNameText = "Abr - Jul 2027",
						canEdit = false
					),
					termItem(
						termId = "2027-JUL_AUG",
						shortNameText = "Jul - Ago 2027",
						canEdit = true
					)
				),
				selectedTermId = "2027-APR_JUL",
				onTermSelected = {},
				onEditTermClick = {}
			)
		}

		onAllNodesWithTag(RecordUiTags.EditSyntheticTermButton).assertCountEquals(0)
	}

	private fun termItem(
		termId: String,
		shortNameText: String,
		canEdit: Boolean
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
			canDelete = canEdit,
			canEdit = canEdit,
			attempts = emptyList()
		)
	}
}
