package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PensumGraphTokensUiTest {
	@Test
	fun when_themeIsLight_then_statusPaletteUsesLightAcademicColors() = runTuIndiceUiTest {
		val palette = resolveGraphPalette(isDark = false)

		assertFalse(palette.graphColors.isDark)
		assertEquals(AcademicStatusColors.ApprovedLight, palette.graphColors.approved)
		assertEquals(AcademicStatusColors.AvailableLight, palette.graphColors.available)
		assertEquals(AcademicStatusColors.BlockedLight, palette.graphColors.blocked)
		assertEquals(AcademicStatusColors.AvailableLight, palette.graphColors.canvasNeutral)
		assertEquals(Color(0xFFD8D8D8), palette.graphColors.panelBorder)
		assertEquals(Color(0xFF5F6368), palette.graphColors.selected)
		assertEquals(Color(0xFF5F6368), palette.graphColors.textSecondary)
	}

	@Test
	fun when_themeIsDark_then_statusPaletteUsesDarkAcademicAndSchemeColors() = runTuIndiceUiTest {
		val palette = resolveGraphPalette(isDark = true)

		assertTrue(palette.graphColors.isDark)
		assertEquals(AcademicStatusColors.ApprovedDark, palette.graphColors.approved)
		assertEquals(AcademicStatusColors.AvailableDark, palette.graphColors.available)
		assertEquals(AcademicStatusColors.BlockedDark, palette.graphColors.blocked)
		assertEquals(palette.colorScheme.outlineVariant, palette.graphColors.panelBorder)
		assertEquals(palette.colorScheme.onSurface, palette.graphColors.selected)
		assertEquals(palette.colorScheme.onSurfaceVariant, palette.graphColors.textSecondary)
	}

	@Test
	fun when_anyThemeIsResolved_then_surfacesAndCurrentStatusComeFromTheColorScheme() =
		runTuIndiceUiTest {
			val palette = resolveGraphPalette(isDark = false)
			val panelBackground = palette.colorScheme.surfaceContainerLow

			assertEquals(palette.colorScheme.primary, palette.graphColors.current)
			assertEquals(palette.colorScheme.background, palette.graphColors.screenBackground)
			assertEquals(palette.colorScheme.onSurface, palette.graphColors.textPrimary)
			assertEquals(panelBackground, palette.graphColors.panelBackground)
			assertEquals(panelBackground, palette.graphColors.floatingPanelBackground)
			assertEquals(panelBackground, palette.graphColors.nodeContainer)
			assertEquals(panelBackground, palette.graphColors.controlsBackground)
			assertEquals(panelBackground, palette.graphColors.minimapBackground)
		}
}

private class GraphPalette(
	val graphColors: PensumGraphColors,
	val colorScheme: ColorScheme
)

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.resolveGraphPalette(isDark: Boolean): GraphPalette {
	var palette: GraphPalette? = null

	setTuIndiceTestContent {
		CompositionLocalProvider(TuIndiceDarkTheme.Local provides isDark) {
			palette = GraphPalette(
				graphColors = pensumGraphColors(),
				colorScheme = MaterialTheme.colorScheme
			)
		}
	}
	waitForIdle()

	return checkNotNull(palette)
}
