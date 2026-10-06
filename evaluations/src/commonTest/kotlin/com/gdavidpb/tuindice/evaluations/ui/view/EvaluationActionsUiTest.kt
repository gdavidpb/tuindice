package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The colours and the rounded corners of each action are only drawn: nothing here reads them.
@OptIn(ExperimentalTestApi::class)
class EvaluationActionsUiTest {
	@Test
	fun when_rendered_then_editComesBeforeDelete_andEachReadsItsLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationActions(
				modifier = Modifier
					.width(224.dp)
					.height(72.dp),
				onEdit = {},
				onDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeEditAction)
		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).assertTextEquals("Modificar")
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).assertTextEquals("Eliminar")

		val edit = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).getUnclippedBoundsInRoot()
		val delete = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).getUnclippedBoundsInRoot()

		assertTrue(edit.right <= delete.left, "edit goes before delete")
	}

	@Test
	fun when_editIsTapped_then_onlyTheEditCallbackIsInvoked() = runTuIndiceUiTest {
		var editClicks = 0
		var deleteClicks = 0

		setTuIndiceTestContent {
			EvaluationActions(
				modifier = Modifier
					.width(224.dp)
					.height(72.dp),
				onEdit = { editClicks++ },
				onDelete = { deleteClicks++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeEditAction)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).performClick()

		assertEquals(1, editClicks)
		assertEquals(0, deleteClicks)
	}

	@Test
	fun when_deleteIsTapped_then_onlyTheDeleteCallbackIsInvoked() = runTuIndiceUiTest {
		var editClicks = 0
		var deleteClicks = 0

		setTuIndiceTestContent {
			EvaluationActions(
				modifier = Modifier
					.width(224.dp)
					.height(72.dp),
				onEdit = { editClicks++ },
				onDelete = { deleteClicks++ }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).performClick()

		assertEquals(0, editClicks)
		assertEquals(1, deleteClicks)
	}
}
