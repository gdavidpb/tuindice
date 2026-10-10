package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.testing.sampleApprovedPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleBlockedPensumNode
import com.gdavidpb.tuindice.pensum.testing.toSampleRelationItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumSubjectRelationRowUiTest {
	@Test
	fun when_rowIsRendered_then_exposesCodeNameStatusAndOpenAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectRelationRow(
				item = sampleApprovedPensumNode().toSampleRelationItem(),
				testTag = ROW_TAG,
				onClick = {}
			)
		}

		onNodeWithTag(ROW_TAG)
			.assert(hasText("EE1111"))
			.assert(hasText("Electiva General"))
			.assert(hasText("Aprobada"))
			.assertContentDescriptionEquals("Abrir EE1111")
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
		onAllNodesWithTag(STATUS_TAG, useUnmergedTree = true).assertCountEquals(0)
	}

	@Test
	fun when_statusTestTagIsProvided_then_tagWrapsTheStatusLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectRelationRow(
				item = sampleBlockedPensumNode().toSampleRelationItem(),
				testTag = ROW_TAG,
				statusTestTag = STATUS_TAG,
				onClick = {}
			)
		}

		onNodeWithTag(STATUS_TAG, useUnmergedTree = true).assertExists()
		onNode(
			matcher = hasText("Bloqueada") and hasParent(hasTestTag(STATUS_TAG)),
			useUnmergedTree = true
		).assertExists()
	}

	@Test
	fun when_rowIsTapped_then_invokesOnClick() = runTuIndiceUiTest {
		var clickCount = 0

		setTuIndiceTestContent {
			PensumSubjectRelationRow(
				item = sampleApprovedPensumNode().toSampleRelationItem(),
				testTag = ROW_TAG,
				onClick = { clickCount += 1 }
			)
		}

		onNodeWithTag(ROW_TAG).performClick()

		runOnIdle { assertEquals(1, clickCount) }
	}
}

private const val ROW_TAG = "pensum_subject_relation_row"
private const val STATUS_TAG = "pensum_subject_relation_row_status"
