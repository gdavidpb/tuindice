package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
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

@OptIn(ExperimentalTestApi::class)
class EvaluationActionsContainerUiTest {
	@Test
	fun when_containerIsWiderThanTheActions_then_actionsKeepTheirWidth_atTheEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(
				modifier = Modifier
					.width(CONTAINER_WIDTH)
					.height(CONTAINER_HEIGHT)
			) {
				EvaluationActionsContainer(
					modifier = Modifier
						.matchParentSize()
						.testTag(CONTAINER_TAG),
					onEdit = {},
					onDelete = {}
				)
			}
		}

		assertNodeVisible(CONTAINER_TAG)
		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeEditAction)
		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction)

		// The two actions share the fixed width of the revealed area and fill its height.
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction)
			.assertWidthIsEqualTo(ActionsWidth / 2)
			.assertHeightIsEqualTo(CONTAINER_HEIGHT)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction)
			.assertWidthIsEqualTo(ActionsWidth / 2)
			.assertHeightIsEqualTo(CONTAINER_HEIGHT)

		val container = onNodeWithTag(CONTAINER_TAG).getUnclippedBoundsInRoot()
		val edit = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).getUnclippedBoundsInRoot()
		val delete = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).getUnclippedBoundsInRoot()

		assertEquals(container.right, delete.right, "the actions end where the container ends")
		assertEquals(container.right - ActionsWidth, edit.left, "the actions take only their own width")
	}

	@Test
	fun when_anActionIsTapped_then_theContainerForwardsItToItsOwnCallback() = runTuIndiceUiTest {
		val taps = mutableListOf<String>()

		setTuIndiceTestContent {
			Box(
				modifier = Modifier
					.width(CONTAINER_WIDTH)
					.height(CONTAINER_HEIGHT)
			) {
				EvaluationActionsContainer(
					modifier = Modifier.matchParentSize(),
					onEdit = { taps += "edit" },
					onDelete = { taps += "delete" }
				)
			}
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).performClick()

		assertEquals(listOf("delete", "edit"), taps)
	}

	private companion object {
		const val CONTAINER_TAG = "evaluation_actions_container_under_test"

		val CONTAINER_WIDTH = 344.dp
		val CONTAINER_HEIGHT = 72.dp
	}
}
