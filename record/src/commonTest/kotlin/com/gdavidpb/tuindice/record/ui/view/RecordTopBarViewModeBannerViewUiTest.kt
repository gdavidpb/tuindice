package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecordTopBarViewModeBannerViewUiTest {
	@Test
	fun when_simulationModeIsDisplayed_then_bannerShowsModeLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordTopBarViewModeBannerView(
				selectedMode = RecordViewMode.Simulation
			)
		}

		assertNodeVisible(RecordUiTags.TopBarViewModeBanner)
		onNodeWithText("Modo Simulación").assertIsDisplayed()
	}
}
