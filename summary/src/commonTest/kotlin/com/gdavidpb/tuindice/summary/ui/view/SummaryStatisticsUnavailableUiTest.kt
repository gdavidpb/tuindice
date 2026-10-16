package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsUnavailableUiTest {
	@Test
	fun when_theRecordIsNotReady_then_itsTitleAndMessageAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsUnavailable(
				title = "Estadísticas no disponibles",
				message = "No pudimos leer tu registro en este momento."
			)
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsUnavailableTitle).assertTextEquals("Estadísticas no disponibles")
		onNodeWithTag(SummaryUiTags.StatisticsUnavailableMessage)
			.assertTextEquals("No pudimos leer tu registro en este momento.")
	}

	@Test
	fun when_theRecordIsNotReady_then_nothingCanBeRetried() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsUnavailable(
				title = "Estadísticas no disponibles",
				message = "No pudimos leer tu registro en este momento."
			)
		}
		waitForIdle()

		onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick), useUnmergedTree = true)
			.assertCountEquals(0)
	}
}
