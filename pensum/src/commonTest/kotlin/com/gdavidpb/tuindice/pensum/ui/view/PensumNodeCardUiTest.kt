package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumFulfilledSubjectItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.testing.sampleApprovedPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleAvailablePensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleBlockedPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNode
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumNodeCardUiTest {
	@Test
	fun when_nodeIsRendered_then_showsCodeNameAndCreditsWithoutFulfilledCode() = runTuIndiceUiTest {
		setNodeCardContent(node = sampleCurrentPensumNode())

		onNodeWithText("CI4325").assertExists()
		onNodeWithText("Interfaces con el Usuario").assertExists()
		onNodeWithText("4 UC").assertExists()
		onAllNodesWithText("LL1111").assertCountEquals(0)
	}

	@Test
	fun when_nodeHasFulfilledSubject_then_showsFulfilledCodeAndDisplayName() = runTuIndiceUiTest {
		val node = sampleCurrentPensumNode().copy(
			displayName = "Lenguaje I",
			fulfilledSubject = PensumFulfilledSubjectItem(code = "LL1111", name = "Lenguaje I")
		)

		setNodeCardContent(node = node)

		onNodeWithText("CI4325").assertExists()
		onNodeWithText("LL1111").assertExists()
		onNodeWithText("Lenguaje I").assertExists()
		onAllNodesWithText("Interfaces con el Usuario").assertCountEquals(0)
	}

	@Test
	fun when_statusVaries_then_stateDescriptionUsesTheLegendWording() = runTuIndiceUiTest {
		val nodesByDescription = mapOf(
			"Aprobada" to sampleApprovedPensumNode(),
			"En curso" to sampleCurrentPensumNode(),
			"Disponible" to sampleAvailablePensumNode(),
			"Bloqueada" to sampleBlockedPensumNode()
		)

		setTuIndiceTestContent {
			nodesByDescription.values.forEach { node ->
				PensumNodeCard(
					node = node,
					isSelected = false,
					isRequirementHighlighted = false,
					isUnlockHighlighted = false,
					modifier = Modifier
						.size(width = 190.dp, height = 144.dp)
						.testTag(node.id)
				)
			}
		}

		nodesByDescription.forEach { (description, node) ->
			onNodeWithTag(node.id).assert(
				SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, description)
			)
		}
	}
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setNodeCardContent(node: PensumNodeItem) {
	setTuIndiceTestContent {
		PensumNodeCard(
			node = node,
			isSelected = true,
			isRequirementHighlighted = false,
			isUnlockHighlighted = false,
			modifier = Modifier.size(width = 190.dp, height = 144.dp)
		)
	}
}
