package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecordTopBarViewModeBannerViewUiTest {
	@Test
	fun when_infoButtonTapped_then_showsBriefProjectionTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordTopBarViewModeBannerView(
				selectedMode = RecordViewMode.Working
			)
		}

		onNodeWithTag(RecordUiTags.TopBarViewModeInfoButton).performClick()

		onNodeWithText("Simula cambios en tus notas. No modifica el historial oficial.")
			.assertIsDisplayed()
	}

	@Test
	fun when_bannerLeavesComposition_then_tooltipIsHidden() = runTuIndiceUiTest {
		val isBannerVisible = mutableStateOf(true)

		setTuIndiceTestContent {
			if (isBannerVisible.value) {
				RecordTopBarViewModeBannerView(
					selectedMode = RecordViewMode.Official
				)
			}
		}

		onNodeWithTag(RecordUiTags.TopBarViewModeInfoButton).performClick()
		onNodeWithText("Notas registradas por la universidad. Solo lectura.")
			.assertIsDisplayed()

		runOnIdle {
			isBannerVisible.value = false
		}

		assertNodeHidden(RecordUiTags.ViewModeInfoMessage)
	}
}
