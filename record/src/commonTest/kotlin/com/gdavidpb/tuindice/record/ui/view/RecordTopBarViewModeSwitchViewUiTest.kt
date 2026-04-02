package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(ExperimentalTestApi::class)
class RecordTopBarViewModeSwitchViewUiTest {
	@Test
	fun when_switchIsTapped_then_togglesToAlternateMode() = runTuIndiceUiTest {
		val selectedModes = mutableListOf<RecordViewMode>()

		setTuIndiceTestContent {
			RecordTopBarViewModeSwitchView(
				selectedMode = RecordViewMode.Simulation,
				onModeSelected = { mode ->
					selectedModes += mode
				}
			)
		}

		assertNodeVisible(RecordUiTags.TopBarViewModeSwitch)
		assertNodeVisible(RecordUiTags.TopBarViewModeButton)

		onNodeWithTag(RecordUiTags.TopBarViewModeButton).performClick()
		waitUntil(timeoutMillis = 2_000) {
			selectedModes.isNotEmpty()
		}

		assertContentEquals(
			expected = listOf(RecordViewMode.Official),
			actual = selectedModes
		)
	}
}
