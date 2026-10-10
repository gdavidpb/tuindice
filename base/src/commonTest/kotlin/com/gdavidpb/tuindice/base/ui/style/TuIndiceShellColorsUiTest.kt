package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TuIndiceShellColorsUiTest {
	@Test
	fun when_colorSchemeIsProvided_then_shellColorsMapToTheirSchemeRoles() = runTuIndiceUiTest {
		var shellColors: ShellColors? = null

		setTuIndiceTestContent {
			MaterialTheme(colorScheme = DayScheme) {
				shellColors = readShellColors()
			}
		}

		runOnIdle {
			assertEquals(
				expected = ShellColors(
					topBarContainer = DaySurface,
					topBarContent = DayOnSurface,
					bottomBarContainer = DayOnSecondary,
					bottomBarIndicator = DaySecondaryContainer
				),
				actual = shellColors
			)
		}
	}

	@Test
	fun when_colorSchemeChanges_then_shellColorsFollowTheNewScheme() = runTuIndiceUiTest {
		val colorScheme = mutableStateOf(DayScheme)
		var shellColors: ShellColors? = null

		setTuIndiceTestContent {
			MaterialTheme(colorScheme = colorScheme.value) {
				shellColors = readShellColors()
			}
		}

		runOnIdle {
			assertEquals(DaySurface, shellColors?.topBarContainer)
			colorScheme.value = NightScheme
		}

		runOnIdle {
			assertEquals(
				expected = ShellColors(
					topBarContainer = NightSurface,
					topBarContent = NightOnSurface,
					bottomBarContainer = NightOnSecondary,
					bottomBarIndicator = NightSecondaryContainer
				),
				actual = shellColors
			)
		}
	}

	@Composable
	private fun readShellColors(): ShellColors {
		return ShellColors(
			topBarContainer = TuIndiceShellColors.topBarContainer(),
			topBarContent = TuIndiceShellColors.topBarContent(),
			bottomBarContainer = TuIndiceShellColors.bottomBarContainer(),
			bottomBarIndicator = TuIndiceShellColors.bottomBarIndicator()
		)
	}

	private data class ShellColors(
		val topBarContainer: Color,
		val topBarContent: Color,
		val bottomBarContainer: Color,
		val bottomBarIndicator: Color
	)

	// Every role gets its own value, so a shell color wired to the wrong role cannot pass by coincidence.
	private companion object {
		val DaySurface = Color(0xFF101112)
		val DayOnSurface = Color(0xFF202122)
		val DayOnSecondary = Color(0xFF303132)
		val DaySecondaryContainer = Color(0xFF404142)
		val NightSurface = Color(0xFF505152)
		val NightOnSurface = Color(0xFF606162)
		val NightOnSecondary = Color(0xFF707172)
		val NightSecondaryContainer = Color(0xFF808182)

		val DayScheme: ColorScheme = lightColorScheme(
			surface = DaySurface,
			onSurface = DayOnSurface,
			onSecondary = DayOnSecondary,
			secondaryContainer = DaySecondaryContainer
		)
		val NightScheme: ColorScheme = darkColorScheme(
			surface = NightSurface,
			onSurface = NightOnSurface,
			onSecondary = NightOnSecondary,
			secondaryContainer = NightSecondaryContainer
		)
	}
}
