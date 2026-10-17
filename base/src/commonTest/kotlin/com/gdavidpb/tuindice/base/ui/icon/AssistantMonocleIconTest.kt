package com.gdavidpb.tuindice.base.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.VectorPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AssistantMonocleIconTest {
	@Test
	fun when_lightAndDarkVariantsAreBuilt_then_onlyTheSparkleKeepsItsColor() {
		val light = assistantMonocleParts(dark = false)
		val dark = assistantMonocleParts(dark = true)

		// The glass, the ring and the three chain links follow the theme; the amber sparkle is the same in both.
		light.zip(dark).dropLast(1).forEach { (lightPart, darkPart) ->
			assertNotEquals(lightPart.paint(), darkPart.paint())
		}
		assertEquals(AMBER, light.last().paint())
		assertEquals(AMBER, dark.last().paint())
	}

	@Test
	fun when_built_then_itHasSixPathsAndEachOneIsPainted() {
		val parts = assistantMonocleParts(dark = false)

		assertEquals(SIX_PATHS, parts.size)
		assertTrue(parts.all { part -> part.fill != null || part.stroke != null })
	}

	private fun assistantMonocleParts(dark: Boolean): List<VectorPath> =
		assistantMonocleIcon(dark = dark).root.filterIsInstance<VectorPath>()

	private fun VectorPath.paint(): Color = ((fill ?: stroke) as SolidColor).value

	private companion object {
		const val SIX_PATHS = 6
		val AMBER = Color(0xFFFABD00)
	}
}
