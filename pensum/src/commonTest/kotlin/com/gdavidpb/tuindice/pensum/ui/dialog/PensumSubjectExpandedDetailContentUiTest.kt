package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.presentation.model.PensumFulfilledSubjectItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_APPROVED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_AVAILABLE_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_CURRENT_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNodeWithRelations
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
class PensumSubjectExpandedDetailContentUiTest {
	@Test
	fun when_nodeOnlyHasFulfilledSubject_then_showsItWithoutRouteOrCorequisites() = runTuIndiceUiTest {
		val fulfilledSubject = PensumFulfilledSubjectItem(code = "LL1111", name = "Lenguaje I")
		val node = sampleCurrentPensumNode().let { current ->
			current.copy(detail = current.detail.copy(fulfilledSubject = fulfilledSubject))
		}

		setExpandedDetailContent(node = node)

		onNodeWithText("Cursada como").assertExists()
		onNodeWithText("LL1111").assertExists()
		onNodeWithText("Lenguaje I").assertExists()
		assertNodeHidden(PensumUiTags.SubjectDetailRouteContext)
		assertNodeHidden(PensumUiTags.SubjectDetailCorequisites)
	}

	@Test
	fun when_nodeOnlyHasCorequisites_then_routeContextIsNotRendered() = runTuIndiceUiTest {
		val node = sampleCurrentPensumNodeWithRelations().let { current ->
			current.copy(
				detail = current.detail.copy(requirements = emptyList(), unlocks = emptyList())
			)
		}

		setExpandedDetailContent(node = node)

		assertNodeVisible(PensumUiTags.SubjectDetailCorequisites)
		assertNodeVisible(PensumUiTags.subjectDetailCorequisite(SAMPLE_AVAILABLE_NODE_ID))
		assertNodeHidden(PensumUiTags.SubjectDetailRouteContext)
		assertNodeHidden(PensumUiTags.SubjectDetailRequirements)
	}

	@Test
	fun when_nodeHasRequirements_then_routeContextTagsThemAsRequirementRows() = runTuIndiceUiTest {
		setExpandedDetailContent(node = sampleCurrentPensumNodeWithRelations())

		assertNodeVisible(PensumUiTags.SubjectDetailRouteContext)
		assertNodeVisible(PensumUiTags.SubjectDetailRequirements)
		assertNodeVisible(PensumUiTags.subjectDetailRequirement(SAMPLE_APPROVED_NODE_ID))
		onNodeWithTag(PensumUiTags.SubjectDetailUnlocks).assertExists()
		onNodeWithText("Cursada como").assertDoesNotExist()
	}

	@Test
	fun when_requirementIsTapped_then_reportsBackwardNavigationFromTheNode() = runTuIndiceUiTest {
		val targets = mutableListOf<PensumSubjectDetailNavigationTarget>()
		setExpandedDetailContent(
			node = sampleCurrentPensumNodeWithRelations(),
			onRelatedSubjectClick = { target -> targets += target }
		)

		onNodeWithTag(PensumUiTags.subjectDetailRequirement(SAMPLE_APPROVED_NODE_ID)).performClick()

		runOnIdle {
			assertEquals(
				listOf(
					PensumSubjectDetailNavigationTarget(
						nodeId = SAMPLE_APPROVED_NODE_ID,
						originNodeId = SAMPLE_CURRENT_NODE_ID,
						direction = PensumSubjectDetailNavigationDirection.Backward
					)
				),
				targets
			)
		}
	}
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setExpandedDetailContent(
	node: PensumNodeItem,
	onRelatedSubjectClick: (PensumSubjectDetailNavigationTarget) -> Unit = {}
) {
	setTuIndiceTestContent {
		PensumSubjectExpandedDetailContent(
			node = node,
			navigationOriginNodeId = null,
			navigationDirection = null,
			onRelatedSubjectClick = onRelatedSubjectClick
		)
	}
}
