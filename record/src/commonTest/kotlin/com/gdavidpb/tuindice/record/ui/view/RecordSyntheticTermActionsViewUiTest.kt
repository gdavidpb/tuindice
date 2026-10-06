package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordSyntheticTermActionsViewUiTest {
	@Test
	fun when_theActionsAreDrawn_then_editSitsAboveDelete_andEachIsReadByWhatItDoes() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordSyntheticTermActionsView(termId = "synthetic-2027", onEditClick = {}, onDeleteClick = {})
		}

		assertNodeVisible(RecordUiTags.EditSyntheticTermButton)
		assertNodeVisible(RecordUiTags.DeleteSyntheticTermButton)
		// Two icons with no label on screen: the description is all that tells them apart aloud.
		onNodeWithTag(RecordUiTags.EditSyntheticTermButton).assertContentDescriptionEquals("Modificar trimestre")
		onNodeWithTag(RecordUiTags.DeleteSyntheticTermButton).assertContentDescriptionEquals("Eliminar trimestre")

		val edit = onNodeWithTag(RecordUiTags.EditSyntheticTermButton).getUnclippedBoundsInRoot()
		val delete = onNodeWithTag(RecordUiTags.DeleteSyntheticTermButton).getUnclippedBoundsInRoot()

		assertTrue(edit.bottom <= delete.top, "the destructive action is the lower one")
	}

	@Test
	fun when_editIsTapped_then_theTermIsReportedForEditing_andNothingIsDeleted() = runTuIndiceUiTest {
		val edited = mutableListOf<String>()
		val deleted = mutableListOf<String>()

		setTuIndiceTestContent {
			RecordSyntheticTermActionsView(
				termId = "synthetic-2027",
				onEditClick = { termId -> edited += termId },
				onDeleteClick = { termId -> deleted += termId }
			)
		}

		onNodeWithTag(RecordUiTags.EditSyntheticTermButton).performClick()

		assertEquals(listOf("synthetic-2027"), edited)
		assertTrue(deleted.isEmpty())
	}

	@Test
	fun when_deleteIsTapped_then_theTermIsReportedForDeleting_andNothingIsEdited() = runTuIndiceUiTest {
		val edited = mutableListOf<String>()
		val deleted = mutableListOf<String>()

		setTuIndiceTestContent {
			RecordSyntheticTermActionsView(
				termId = "synthetic-2028",
				onEditClick = { termId -> edited += termId },
				onDeleteClick = { termId -> deleted += termId }
			)
		}

		onNodeWithTag(RecordUiTags.DeleteSyntheticTermButton).performClick()

		assertEquals(listOf("synthetic-2028"), deleted)
		assertTrue(edited.isEmpty())
	}
}
