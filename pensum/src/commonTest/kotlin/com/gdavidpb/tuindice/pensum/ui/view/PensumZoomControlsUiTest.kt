package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumZoomControlsUiTest {
	@Test
	fun when_optionalControlsAreHidden_then_onlyZoomButtonsFillTheColumn() = runTuIndiceUiTest {
		setZoomControlsContent(events = mutableListOf(), areOptionalControlsVisible = false)

		onNodeWithTag(PensumUiTags.ZoomIn).assertContentDescriptionEquals("Acercar")
		onNodeWithTag(PensumUiTags.ZoomOut).assertContentDescriptionEquals("Alejar")
		assertNodeHidden(PensumUiTags.FocusProgress)
		assertNodeHidden(PensumUiTags.FitToScreen)
		assertNodeHidden(PensumUiTags.MinimapToggle)
		onNodeWithTag(CONTROLS_TAG)
			.assertWidthIsEqualTo(48.dp)
			.assertHeightIsEqualTo(ZoomControlButtonHeight * 2)
	}

	@Test
	fun when_optionalControlsAreVisible_then_columnGrowsToFiveDescribedButtons() = runTuIndiceUiTest {
		setZoomControlsContent(events = mutableListOf(), areOptionalControlsVisible = true)

		onNodeWithTag(PensumUiTags.FocusProgress).assertContentDescriptionEquals("Volver a mi avance")
		onNodeWithTag(PensumUiTags.FitToScreen).assertContentDescriptionEquals("Ajustar al contenido")
		onNodeWithTag(PensumUiTags.MinimapToggle).assertContentDescriptionEquals("Mostrar mapa")
		onNodeWithTag(PensumUiTags.ZoomIn).assertContentDescriptionEquals("Acercar")
		onNodeWithTag(PensumUiTags.ZoomOut).assertContentDescriptionEquals("Alejar")
		onNodeWithTag(CONTROLS_TAG).assertHeightIsEqualTo(ZoomControlButtonHeight * 5)
	}

	@Test
	fun when_minimapIsVisible_then_toggleOffersToHideIt() = runTuIndiceUiTest {
		setZoomControlsContent(
			events = mutableListOf(),
			areOptionalControlsVisible = true,
			isMinimapVisible = true
		)

		onNodeWithTag(PensumUiTags.MinimapToggle).assertContentDescriptionEquals("Ocultar mapa")
	}

	@Test
	fun when_eachControlIsTapped_then_onlyItsOwnCallbackFires() = runTuIndiceUiTest {
		val events = mutableListOf<String>()
		setZoomControlsContent(events = events, areOptionalControlsVisible = true)

		onNodeWithTag(PensumUiTags.FocusProgress).performClick()
		onNodeWithTag(PensumUiTags.FitToScreen).performClick()
		onNodeWithTag(PensumUiTags.MinimapToggle).performClick()
		onNodeWithTag(PensumUiTags.ZoomIn).performClick()
		onNodeWithTag(PensumUiTags.ZoomOut).performClick()

		runOnIdle {
			assertEquals(
				listOf("focusProgress", "fitToScreen", "toggleMinimap", "zoomIn", "zoomOut"),
				events
			)
		}
	}
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setZoomControlsContent(
	events: MutableList<String>,
	areOptionalControlsVisible: Boolean,
	isMinimapVisible: Boolean = false
) {
	setTuIndiceTestContent {
		PensumZoomControls(
			isCurrentFocusVisible = areOptionalControlsVisible,
			isMinimapToggleVisible = areOptionalControlsVisible,
			isMinimapVisible = isMinimapVisible,
			isFitToScreenVisible = areOptionalControlsVisible,
			onFocusProgress = { events += "focusProgress" },
			onFitToScreen = { events += "fitToScreen" },
			onToggleMinimap = { events += "toggleMinimap" },
			onZoomIn = { events += "zoomIn" },
			onZoomOut = { events += "zoomOut" },
			modifier = Modifier.testTag(CONTROLS_TAG)
		)
	}
}

private const val CONTROLS_TAG = "pensum_zoom_controls"
