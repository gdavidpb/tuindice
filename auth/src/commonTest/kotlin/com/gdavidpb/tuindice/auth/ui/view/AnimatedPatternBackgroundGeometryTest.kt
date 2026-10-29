package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AnimatedPatternBackgroundGeometryTest {
	private val tileWidthPx = 40
	private val tileHeightPx = 30

	// The corners of the tiles that the pattern draws for an offset and a viewport.
	private fun tilesOf(
		animatedX: Int,
		viewportWidth: Float,
		viewportHeight: Float
	): Set<IntOffset> {
		val tiles = mutableSetOf<IntOffset>()

		forEachPatternTile(
			viewport = Size(viewportWidth, viewportHeight),
			animatedX = animatedX,
			tileWidthPx = tileWidthPx,
			tileHeightPx = tileHeightPx
		) { x, y ->
			tiles += IntOffset(x, y)
		}

		return tiles
	}

	private fun gridOf(columns: List<Int>, rows: List<Int>): Set<IntOffset> {
		return columns.flatMap { x -> rows.map { y -> IntOffset(x, y) } }.toSet()
	}

	private fun reachesViewport(tile: IntOffset, viewportWidth: Float, viewportHeight: Float): Boolean {
		return tile.x < viewportWidth && tile.x + tileWidthPx > 0 && tile.y < viewportHeight && tile.y + tileHeightPx > 0
	}

	private fun isCovered(tiles: Set<IntOffset>, px: Int, py: Int): Boolean {
		return tiles.any { tile ->
			px >= tile.x && px < tile.x + tileWidthPx && py >= tile.y && py < tile.y + tileHeightPx
		}
	}

	// Every pixel of the viewport lies in some tile, and no tile lies outside the viewport.
	private fun assertCoveredAndInside(tiles: Set<IntOffset>, viewport: Size, animatedX: Int) {
		tiles.forEach { tile ->
			assertTrue(
				reachesViewport(tile, viewport.width, viewport.height),
				"tile $tile is outside the viewport $viewport at offset $animatedX"
			)
		}

		for (px in 0 until viewport.width.toInt()) {
			for (py in 0 until viewport.height.toInt()) {
				if (!isCovered(tiles, px, py)) fail("pixel ($px, $py) is not covered at offset $animatedX")
			}
		}
	}

	@Test
	fun when_theOffsetIsZero_then_theTilesStartAtTheLeftAndTopEdges() {
		// Three columns and three rows of 40x30 tiles fit in 120x90. The column at 120 starts on the edge: not drawn.
		assertEquals(
			expected = gridOf(columns = listOf(0, 40, 80), rows = listOf(0, 30, 60)),
			actual = tilesOf(animatedX = 0, viewportWidth = 120f, viewportHeight = 90f)
		)
	}

	@Test
	fun when_theOffsetIsInsideATile_then_theColumnBehindTheEdgeIsDrawnToo() {
		// The column at -30 still covers the viewport from 0 to 10.
		assertEquals(
			expected = gridOf(columns = listOf(-30, 10, 50, 90), rows = listOf(0, 30, 60)),
			actual = tilesOf(animatedX = 10, viewportWidth = 120f, viewportHeight = 90f)
		)
	}

	@Test
	fun when_theOffsetIsAFullTile_then_thePatternRestartsAtTheEdge() {
		assertEquals(
			expected = tilesOf(animatedX = 0, viewportWidth = 120f, viewportHeight = 90f),
			actual = tilesOf(animatedX = tileWidthPx, viewportWidth = 120f, viewportHeight = 90f)
		)
	}

	@Test
	fun when_theViewportIsSmallerThanATile_then_oneTileCoversIt() {
		assertEquals(
			expected = setOf(IntOffset(0, 0)),
			actual = tilesOf(animatedX = 0, viewportWidth = 25f, viewportHeight = 20f)
		)
	}

	@Test
	fun when_theViewportIsEmpty_then_noTileIsDrawn() {
		assertTrue(tilesOf(animatedX = 10, viewportWidth = 0f, viewportHeight = 0f).isEmpty())
	}

	@Test
	fun when_theOffsetMovesThroughATile_then_everyPixelIsCoveredAndNoTileIsOutside() {
		val viewports = listOf(Size(120f, 90f), Size(41f, 31f), Size(40f, 30f), Size(1f, 1f))

		for (viewport in viewports) {
			for (animatedX in 0..tileWidthPx) {
				val tiles = tilesOf(animatedX, viewport.width, viewport.height)

				assertCoveredAndInside(tiles, viewport, animatedX)
			}
		}
	}
}
