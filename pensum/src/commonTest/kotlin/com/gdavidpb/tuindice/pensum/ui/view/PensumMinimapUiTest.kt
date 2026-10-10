package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PensumMinimapUiTest {
	@Test
	fun when_minimapIsRendered_then_occupiesItsDeclaredSize() = runTuIndiceUiTest {
		setMinimapContent(viewportCenters = mutableListOf())

		assertNodeVisible(PensumUiTags.Minimap)
		onNodeWithTag(MINIMAP_HOST_TAG)
			.assertWidthIsEqualTo(MinimapWidth)
			.assertHeightIsEqualTo(MinimapHeight)
	}

	@Test
	fun when_minimapCenterIsTapped_then_reportsTheCenterOfTheCanvas() = runTuIndiceUiTest {
		val viewportCenters = mutableListOf<Offset>()
		setMinimapContent(viewportCenters = viewportCenters)

		onNodeWithTag(PensumUiTags.Minimap).performTouchInput { click(center) }
		waitForIdle()

		assertEquals(1, viewportCenters.size)
		assertOffsetNear(
			expected = Offset(CANVAS_WIDTH / 2f, CANVAS_HEIGHT / 2f),
			actual = viewportCenters.single()
		)
	}

	@Test
	fun when_minimapIsDraggedToItsCorner_then_reportsCanvasPointsUpToThatCorner() = runTuIndiceUiTest {
		val viewportCenters = mutableListOf<Offset>()
		setMinimapContent(viewportCenters = viewportCenters)

		onNodeWithTag(PensumUiTags.Minimap).performTouchInput {
			swipe(start = center, end = bottomRight, durationMillis = 200)
		}
		waitForIdle()

		assertTrue(viewportCenters.size > 1, "Expected one report per drag step.")
		assertOffsetNear(expected = Offset(CANVAS_WIDTH, CANVAS_HEIGHT), actual = viewportCenters.last())
		assertTrue(
			actual = viewportCenters.zipWithNext().all { (previous, next) ->
				next.x >= previous.x && next.y >= previous.y
			},
			message = "Expected the reported points to follow the drag towards the corner."
		)
		assertTrue(
			actual = viewportCenters.all { point -> point.x <= CANVAS_WIDTH && point.y <= CANVAS_HEIGHT },
			message = "Expected every reported point to stay inside the canvas."
		)
	}
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setMinimapContent(viewportCenters: MutableList<Offset>) {
	val model = samplePensumScreenModel()

	setTuIndiceTestContent {
		Box(modifier = Modifier.testTag(MINIMAP_HOST_TAG)) {
			PensumMinimap(
				model = model,
				edgeRoutes = emptyMap(),
				scale = 1f,
				offset = Offset.Zero,
				viewportSizePx = Size(width = 360f, height = 640f),
				selectedRequirementEdgeIds = emptySet(),
				selectedUnlockEdgeIds = emptySet(),
				selectedAvailableUnlockEdgeIds = emptySet(),
				focusedNodeIds = emptySet(),
				isFocusActive = false,
				statusFilteredNodeIds = emptySet(),
				isStatusFilterActive = false,
				densityScale = 1f,
				onViewportCenterChange = { canvasCenter -> viewportCenters += canvasCenter }
			)
		}
	}
}

private fun assertOffsetNear(expected: Offset, actual: Offset) {
	assertTrue(
		actual = abs(expected.x - actual.x) <= OFFSET_TOLERANCE &&
			abs(expected.y - actual.y) <= OFFSET_TOLERANCE,
		message = "Expected a canvas point near $expected but was $actual."
	)
}

private const val MINIMAP_HOST_TAG = "pensum_minimap_host"

// The sample model's canvas, in canvas units.
private const val CANVAS_WIDTH = 520f
private const val CANVAS_HEIGHT = 700f

// One minimap pixel spans about 4 x 8 canvas units, so a tap resolves no finer.
private const val OFFSET_TOLERANCE = 12f
