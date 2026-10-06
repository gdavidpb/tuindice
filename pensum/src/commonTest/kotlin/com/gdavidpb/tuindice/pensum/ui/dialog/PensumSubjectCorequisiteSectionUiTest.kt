package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeRelationshipType
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_APPROVED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_AVAILABLE_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_CURRENT_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.sampleApprovedPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleAvailablePensumNode
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
class PensumSubjectCorequisiteSectionUiTest {
	@Test
	fun when_itemsAreEmpty_then_sectionIsNotRendered() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectCorequisiteSection(
				originNodeId = SAMPLE_CURRENT_NODE_ID,
				items = emptyList(),
				onRelatedSubjectClick = {}
			)
		}

		assertNodeHidden(PensumUiTags.SubjectDetailCorequisites)
		assertNodeHidden(PensumUiTags.SubjectDetailAlsoWith)
	}

	@Test
	fun when_itemsExist_then_showsHeaderAndOneActionableRowPerCorequisite() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectCorequisiteSection(
				originNodeId = SAMPLE_CURRENT_NODE_ID,
				items = sampleCorequisites(),
				onRelatedSubjectClick = {}
			)
		}

		assertNodeVisible(PensumUiTags.SubjectDetailCorequisites)
		onNodeWithTag(PensumUiTags.SubjectDetailAlsoWith).assertTextEquals("También se cursa con")
		onNodeWithTag(PensumUiTags.subjectDetailCorequisite(SAMPLE_AVAILABLE_NODE_ID))
			.assertHasClickAction()
		onNodeWithTag(PensumUiTags.subjectDetailCorequisite(SAMPLE_APPROVED_NODE_ID))
			.assertHasClickAction()
		onNodeWithTag(
			PensumUiTags.subjectDetailRelationStatus(SAMPLE_AVAILABLE_NODE_ID),
			useUnmergedTree = true
		).assertExists()
	}

	@Test
	fun when_corequisiteIsTapped_then_reportsLateralNavigationFromTheOrigin() = runTuIndiceUiTest {
		val targets = mutableListOf<PensumSubjectDetailNavigationTarget>()

		setTuIndiceTestContent {
			PensumSubjectCorequisiteSection(
				originNodeId = SAMPLE_CURRENT_NODE_ID,
				items = sampleCorequisites(),
				onRelatedSubjectClick = { target -> targets += target }
			)
		}

		onNodeWithTag(PensumUiTags.subjectDetailCorequisite(SAMPLE_APPROVED_NODE_ID)).performClick()

		runOnIdle {
			assertEquals(
				listOf(
					PensumSubjectDetailNavigationTarget(
						nodeId = SAMPLE_APPROVED_NODE_ID,
						originNodeId = SAMPLE_CURRENT_NODE_ID,
						direction = PensumSubjectDetailNavigationDirection.Lateral
					)
				),
				targets
			)
		}
	}
}

private fun sampleCorequisites() = listOf(
	sampleAvailablePensumNode().toSampleRelationItem(PensumEdgeRelationshipType.COREQUISITE),
	sampleApprovedPensumNode().toSampleRelationItem(PensumEdgeRelationshipType.COREQUISITE)
)
