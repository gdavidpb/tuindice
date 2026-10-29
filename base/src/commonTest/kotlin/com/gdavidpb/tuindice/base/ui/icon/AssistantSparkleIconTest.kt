package com.gdavidpb.tuindice.base.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.VectorPath
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantSparkleIconTest {
	@Test
	fun when_built_then_itIsFilledInBlack_soTheSurfaceTintsIt() {
		assertEquals(Color.Black, assistantSparkleParts().single().paint())
	}

	@Test
	fun when_built_then_itIsASingleFilledPathInA24UnitViewport() {
		val icon = assistantSparkleIcon()

		assertEquals(VIEWPORT, icon.viewportWidth)
		assertEquals(VIEWPORT, icon.viewportHeight)
		assertEquals(1, assistantSparkleParts().size)
	}

	private fun assistantSparkleParts(): List<VectorPath> =
		assistantSparkleIcon().root.filterIsInstance<VectorPath>()

	private fun VectorPath.paint(): Color = (fill as SolidColor).value

	private companion object {
		const val VIEWPORT = 24f
	}
}
