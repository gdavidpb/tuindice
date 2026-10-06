package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNode
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumSelectedSubjectRouteCardUiTest {
	@Test
	fun when_nodeIsProvided_then_showsSelectedLabelCodeAndName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSelectedSubjectRouteCard(node = sampleCurrentPensumNode())
		}

		assertNodeVisible(PensumUiTags.SubjectDetailSelectedRouteCard)
		onNodeWithText("Materia seleccionada").assertExists()
		onNodeWithText("CI4325").assertExists()
		onNodeWithText("Interfaces con el Usuario").assertExists()
	}

	@Test
	fun when_canvasLabelsDifferFromDetail_then_cardShowsTheDetailCodeAndName() = runTuIndiceUiTest {
		val node = sampleCurrentPensumNode().copy(
			displayCode = "LL1111",
			displayName = "Lenguaje I"
		)

		setTuIndiceTestContent {
			PensumSelectedSubjectRouteCard(node = node)
		}

		onNodeWithText("CI4325").assertExists()
		onNodeWithText("Interfaces con el Usuario").assertExists()
		onAllNodesWithText("LL1111").assertCountEquals(0)
		onAllNodesWithText("Lenguaje I").assertCountEquals(0)
	}
}
