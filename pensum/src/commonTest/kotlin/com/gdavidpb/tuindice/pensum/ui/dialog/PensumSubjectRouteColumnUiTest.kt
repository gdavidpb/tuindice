package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_APPROVED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_BLOCKED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_CURRENT_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.sampleApprovedPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleBlockedPensumNode
import com.gdavidpb.tuindice.pensum.testing.toSampleRelationItem
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationDirection
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationTarget
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumSubjectRouteColumnUiTest {
	@Test
	fun when_columnHasItems_then_showsTitleAndOneTaggedCardPerItem() = runTuIndiceUiTest {
		setRouteColumnContent(items = sampleRouteItems())

		assertNodeVisible(COLUMN_TAG)
		onNodeWithText("Requisitos para").assertExists()
		onNodeWithTag(routeRowTag(SAMPLE_APPROVED_NODE_ID)).assertHasClickAction()
		onNodeWithTag(routeRowTag(SAMPLE_BLOCKED_NODE_ID)).assertHasClickAction()
		onAllNodes(hasClickAction()).assertCountEquals(2)
	}

	@Test
	fun when_columnHasNoItems_then_showsOnlyTheTitle() = runTuIndiceUiTest {
		setRouteColumnContent(items = emptyList())

		assertNodeVisible(COLUMN_TAG)
		onNodeWithText("Requisitos para").assertExists()
		onAllNodes(hasClickAction()).assertCountEquals(0)
	}

	@Test
	fun when_cardIsTapped_then_reportsTargetWithColumnOriginAndDirection() = runTuIndiceUiTest {
		val targets = mutableListOf<PensumSubjectDetailNavigationTarget>()
		setRouteColumnContent(
			items = sampleRouteItems(),
			onRelatedSubjectClick = { target -> targets += target }
		)

		onNodeWithTag(routeRowTag(SAMPLE_BLOCKED_NODE_ID)).performClick()

		runOnIdle {
			assertEquals(
				listOf(
					PensumSubjectDetailNavigationTarget(
						nodeId = SAMPLE_BLOCKED_NODE_ID,
						originNodeId = SAMPLE_CURRENT_NODE_ID,
						direction = PensumSubjectDetailNavigationDirection.Forward
					)
				),
				targets
			)
		}
	}
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setRouteColumnContent(
	items: List<PensumSubjectRelationItem>,
	onRelatedSubjectClick: (PensumSubjectDetailNavigationTarget) -> Unit = {}
) {
	setTuIndiceTestContent {
		PensumSubjectRouteColumn(
			title = "Requisitos para",
			items = items,
			testTag = COLUMN_TAG,
			rowTag = ::routeRowTag,
			originNodeId = SAMPLE_CURRENT_NODE_ID,
			navigationDirection = PensumSubjectDetailNavigationDirection.Forward,
			onRelatedSubjectClick = onRelatedSubjectClick
		)
	}
}

private fun sampleRouteItems() = listOf(
	sampleApprovedPensumNode().toSampleRelationItem(),
	sampleBlockedPensumNode().toSampleRelationItem()
)

private fun routeRowTag(nodeId: String): String = "pensum_route_column_row_$nodeId"

private const val COLUMN_TAG = "pensum_route_column"
