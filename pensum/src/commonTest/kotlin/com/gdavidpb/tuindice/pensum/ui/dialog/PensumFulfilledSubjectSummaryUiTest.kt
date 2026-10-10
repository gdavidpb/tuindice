package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.pensum.presentation.model.PensumFulfilledSubjectItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.testing.samplePensumVisualStyle
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumFulfilledSubjectSummaryUiTest {
	@Test
	fun when_fulfilledSubjectIsProvided_then_showsLabelCodeAndName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumFulfilledSubjectSummary(
				fulfilledSubject = PensumFulfilledSubjectItem(code = "LL1111", name = "Lenguaje I"),
				visualStyle = samplePensumVisualStyle(PensumNodeStatusType.APPROVED)
			)
		}

		onNodeWithText("Cursada como").assertExists()
		onNodeWithText("LL1111").assertExists()
		onNodeWithText("Lenguaje I").assertExists()
	}

	@Test
	fun when_fulfilledSubjectChanges_then_showsOnlyTheNewSubject() = runTuIndiceUiTest {
		val fulfilledSubjectState = mutableStateOf(
			PensumFulfilledSubjectItem(code = "LL1111", name = "Lenguaje I")
		)

		setTuIndiceTestContent {
			PensumFulfilledSubjectSummary(
				fulfilledSubject = fulfilledSubjectState.value,
				visualStyle = samplePensumVisualStyle(PensumNodeStatusType.CURRENT)
			)
		}

		runOnIdle {
			fulfilledSubjectState.value = PensumFulfilledSubjectItem(code = "EP1420", name = "Pasantía")
		}
		waitForIdle()

		onNodeWithText("Cursada como").assertExists()
		onNodeWithText("EP1420").assertExists()
		onNodeWithText("Pasantía").assertExists()
		onAllNodesWithText("LL1111").assertCountEquals(0)
		onAllNodesWithText("Lenguaje I").assertCountEquals(0)
	}
}
