package com.gdavidpb.tuindice.base.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.VectorPath
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantSparkleIconTest {
	@Test
	fun when_lightAndDarkVariantsAreBuilt_then_theAmberFollowsTheTheme() {
		assertEquals(DARK_AMBER, assistantSparkleParts(dark = false).single().paint())
		assertEquals(BRIGHT_AMBER, assistantSparkleParts(dark = true).single().paint())
	}

	@Test
	fun when_built_then_itIsASingleFilledPathInA24UnitViewport() {
		val icon = assistantSparkleIcon(dark = false)

		assertEquals(VIEWPORT, icon.viewportWidth)
		assertEquals(VIEWPORT, icon.viewportHeight)
		assertEquals(1, assistantSparkleParts(dark = false).size)
	}

	private fun assistantSparkleParts(dark: Boolean): List<VectorPath> =
		assistantSparkleIcon(dark = dark).root.filterIsInstance<VectorPath>()

	private fun VectorPath.paint(): Color = (fill as SolidColor).value

	private companion object {
		const val VIEWPORT = 24f
		val DARK_AMBER = Color(0xFF785900)
		val BRIGHT_AMBER = Color(0xFFFABD00)
	}
}
