package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_APPROVED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_BLOCKED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_CURRENT_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.sampleAvailablePensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNodeWithRelations
import com.gdavidpb.tuindice.pensum.testing.toSampleRelationItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationDirection
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationTarget
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumSubjectRouteContextUiTest {
	@Test
	fun when_oneRequirementAndOneUnlock_then_columnsUseSingularTitles() = runTuIndiceUiTest {
		setRouteContextContent(node = sampleCurrentPensumNodeWithRelations())

		assertNodeVisible(PensumUiTags.SubjectDetailRouteContext)
		onNodeWithText("Ruta de esta materia").assertExists()
		onNodeWithText("Requisito").assertExists()
		onNodeWithText("Materia seleccionada").assertExists()
		onNodeWithText("Requisito para").assertExists()
		onNodeWithTag(PensumUiTags.SubjectDetailSelectedRouteCard).assertExists()
	}

	@Test
	fun when_severalRequirementsAndUnlocks_then_columnsUsePluralTitles() = runTuIndiceUiTest {
		val extraItem = sampleAvailablePensumNode().toSampleRelationItem()
		val node = sampleCurrentPensumNodeWithRelations().let { current ->
			current.copy(
				detail = current.detail.copy(
					requirements = current.detail.requirements + extraItem,
					unlocks = current.detail.unlocks + extraItem
				)
			)
		}

		setRouteContextContent(node = node)

		onNodeWithText("Requisitos").assertExists()
		onNodeWithText("Requisitos para").assertExists()
		onAllNodesWithText("Requisito").assertCountEquals(0)
		onAllNodesWithText("Requisito para").assertCountEquals(0)
	}

	@Test
	fun when_thereAreNoRequirements_then_onlySelectedCardAndUnlocksAreShown() = runTuIndiceUiTest {
		val node = sampleCurrentPensumNodeWithRelations().let { current ->
			current.copy(detail = current.detail.copy(requirements = emptyList()))
		}

		setRouteContextContent(node = node)

		assertNodeHidden(PensumUiTags.SubjectDetailRequirements)
		onAllNodesWithText("Requisito").assertCountEquals(0)
		onNodeWithTag(PensumUiTags.SubjectDetailSelectedRouteCard).assertExists()
		onNodeWithTag(PensumUiTags.SubjectDetailUnlocks).assertExists()
		onNodeWithTag(PensumUiTags.subjectDetailUnlock(SAMPLE_BLOCKED_NODE_ID)).assertExists()
	}

	@Test
	fun when_requirementAndUnlockAreTapped_then_reportBackwardAndForwardTargets() = runTuIndiceUiTest {
		val targets = mutableListOf<PensumSubjectDetailNavigationTarget>()
		setRouteContextContent(
			node = sampleCurrentPensumNodeWithRelations(),
			onRelatedSubjectClick = { target -> targets += target }
		)

		onNodeWithTag(REQUIREMENT_ROW_PREFIX + SAMPLE_APPROVED_NODE_ID).performClick()
		onNodeWithTag(PensumUiTags.subjectDetailUnlock(SAMPLE_BLOCKED_NODE_ID))
			.performScrollTo()
			.performClick()

		runOnIdle {
			assertEquals(
				listOf(
					PensumSubjectDetailNavigationTarget(
						nodeId = SAMPLE_APPROVED_NODE_ID,
						originNodeId = SAMPLE_CURRENT_NODE_ID,
						direction = PensumSubjectDetailNavigationDirection.Backward
					),
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
private fun ComposeUiTest.setRouteContextContent(
	node: PensumNodeItem,
	onRelatedSubjectClick: (PensumSubjectDetailNavigationTarget) -> Unit = {}
) {
	setTuIndiceTestContent {
		PensumSubjectRouteContext(
			node = node,
			beforeItems = node.detail.requirements,
			beforeTestTag = PensumUiTags.SubjectDetailRequirements,
			beforeRowTag = { nodeId -> REQUIREMENT_ROW_PREFIX + nodeId },
			afterItems = node.detail.unlocks,
			navigationOriginNodeId = null,
			navigationDirection = null,
			onRelatedSubjectClick = onRelatedSubjectClick
		)
	}
}

private const val REQUIREMENT_ROW_PREFIX = "pensum_route_context_requirement_"
