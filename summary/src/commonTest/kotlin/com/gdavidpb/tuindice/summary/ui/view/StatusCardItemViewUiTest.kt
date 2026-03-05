package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEntry
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StatusCardItemViewUiTest {
	@Test
	fun when_entriesContainZeroValue_then_hidesZeroDistributionText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StatusCardItemView(
				header = "Materias inscritas",
				entries = listOf(
					SummaryEntry(label = "Aprobadas", value = 3, color = Color.Green),
					SummaryEntry(label = "Reprobadas", value = 0, color = Color.Red),
					SummaryEntry(label = "Retiradas", value = 1, color = Color.Gray)
				)
			)
		}

		onNodeWithText("Materias inscritas").assertIsDisplayed()
		onNodeWithText("Aprobadas").assertIsDisplayed()
		onNodeWithText("Reprobadas").assertIsDisplayed()
		onNodeWithText("Retiradas").assertIsDisplayed()
		onNodeWithText("3").assertIsDisplayed()
		onNodeWithText("1").assertIsDisplayed()
		onAllNodesWithText("0").assertCountEquals(0)
	}

	@Test
	fun when_allEntriesAreZero_then_keepsHeaderAndLabelsWithoutDistributionValues() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StatusCardItemView(
				header = "Creditos inscritos",
				entries = listOf(
					SummaryEntry(label = "Aprobados", value = 0, color = Color.Green),
					SummaryEntry(label = "Reprobados", value = 0, color = Color.Red),
					SummaryEntry(label = "Retirados", value = 0, color = Color.Gray)
				)
			)
		}

		onNodeWithText("Creditos inscritos").assertIsDisplayed()
		onNodeWithText("Aprobados").assertIsDisplayed()
		onNodeWithText("Reprobados").assertIsDisplayed()
		onNodeWithText("Retirados").assertIsDisplayed()
		onAllNodesWithText("0").assertCountEquals(0)
	}
}
