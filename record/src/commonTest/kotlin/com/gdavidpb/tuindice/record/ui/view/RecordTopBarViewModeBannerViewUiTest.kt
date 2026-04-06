package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
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

	@Test
	fun when_infoButtonIsTapped_then_displaysExplanationSheetForCurrentMode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordTopBarViewModeBannerView(
				selectedMode = RecordViewMode.Simulation
			)
		}

		assertNodeVisible(RecordUiTags.TopBarViewModeInfoButton)

		onNodeWithTag(RecordUiTags.TopBarViewModeInfoButton).performClick()

		assertNodeVisible(BaseUiTags.ConfirmationDialogSheet)
		assertNodeVisible(RecordUiTags.ViewModeInfoMessage)
		onNodeWithText(
			"Estás viendo una simulación de tu informe académico. Aquí puedes ajustar calificaciones para explorar cómo cambiarían tu promedio y tus acumulados dentro de la app; estos cambios no modifican tu registro oficial en la universidad."
		).assertIsDisplayed()
	}
}
