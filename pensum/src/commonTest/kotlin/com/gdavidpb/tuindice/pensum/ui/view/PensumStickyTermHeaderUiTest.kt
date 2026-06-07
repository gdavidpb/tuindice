package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTermItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PensumStickyTermHeaderUiTest {
	@Test
	fun when_zoomIsBelowStickyHeaderThreshold_then_headerGateIsHidden() {
		assertFalse(
			shouldRenderStickyTermHeader(
				scale = StickyTermHeaderMinZoom - 0.01f,
				isFitToScreen = false
			)
		)
	}

	@Test
	fun when_zoomMeetsStickyHeaderThreshold_then_headerGateIsVisible() {
		assertTrue(
			shouldRenderStickyTermHeader(
				scale = StickyTermHeaderMinZoom,
				isFitToScreen = false
			)
		)
	}

	@Test
	fun when_canvasIsFitToScreen_then_headerGateIsHidden() {
		assertFalse(
			shouldRenderStickyTermHeader(
				scale = StickyTermHeaderMinZoom + 0.2f,
				isFitToScreen = true
			)
		)
	}

	@Test
	fun when_headerIsRenderedAtThreshold_then_headerIsVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val density = LocalDensity.current
			Box(
				modifier = Modifier
					.width(320.dp)
					.height(StickyTermHeaderHeight)
			) {
				PensumStickyTermHeader(
					terms = stickyHeaderTerms(),
					scale = StickyTermHeaderMinZoom,
					offsetX = 0f,
					densityScale = density.density,
					onTermClick = {}
				)
			}
		}

		assertNodeVisible(PensumUiTags.StickyTerms)
		onNodeWithText("1°").assertExists()
		onAllNodesWithText("1° trimestre").assertCountEquals(0)
	}
}

private fun stickyHeaderTerms(): List<PensumTermItem> {
	return listOf(
		PensumTermItem(id = "T1", label = "Primer trimestre", x = 0.0, width = 120.0),
		PensumTermItem(id = "T2", label = "Segundo trimestre", x = 120.0, width = 120.0),
		PensumTermItem(id = "T3", label = "Tercer trimestre", x = 240.0, width = 120.0)
	)
}
