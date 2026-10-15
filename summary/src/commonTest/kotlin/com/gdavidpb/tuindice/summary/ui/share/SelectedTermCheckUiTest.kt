package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SelectedTermCheckUiTest {
	@Test
	fun when_itIsShown_then_itIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SelectedTermCheck()
		}

		onRoot(useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_itMarksAChosenChip_then_theChipKeepsItsLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			FilterChip(
				selected = true,
				onClick = {},
				label = { Text(text = "Ene - Mar 2026") },
				leadingIcon = { SelectedTermCheck() }
			)
		}

		onNodeWithText("Ene - Mar 2026").assertIsDisplayed()
	}
}
