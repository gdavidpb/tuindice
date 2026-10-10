package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AcademicStatusColorsUiTest {
	@Test
	fun when_darkThemeIsForced_then_statusColorsUseTheirDarkVariants() = runTuIndiceUiTest {
		var statusColors: StatusColors? = null

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides true) {
				statusColors = readStatusColors()
			}
		}

		runOnIdle {
			assertEquals(DarkStatusColors, statusColors)
		}
	}

	@Test
	fun when_lightThemeIsForced_then_statusColorsUseTheirLightVariants() = runTuIndiceUiTest {
		var statusColors: StatusColors? = null

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides false) {
				statusColors = readStatusColors()
			}
		}

		runOnIdle {
			assertEquals(LightStatusColors, statusColors)
		}
	}

	@Test
	fun when_forcedThemeChanges_then_statusColorsSwitchVariant() = runTuIndiceUiTest {
		val isDark = mutableStateOf(false)
		var statusColors: StatusColors? = null

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides isDark.value) {
				statusColors = readStatusColors()
			}
		}

		runOnIdle {
			assertEquals(LightStatusColors, statusColors)
			isDark.value = true
		}

		runOnIdle {
			assertEquals(DarkStatusColors, statusColors)
		}
	}

	@Test
	fun when_currentStatusIsResolved_then_itIsTheSchemePrimaryInBothThemes() = runTuIndiceUiTest {
		var currentInDark: Color? = null
		var currentInLight: Color? = null

		setTuIndiceTestContent {
			MaterialTheme(colorScheme = lightColorScheme(primary = SchemePrimary)) {
				CompositionLocalProvider(TuIndiceDarkTheme.Local provides true) {
					currentInDark = AcademicStatusColors.current()
				}
				CompositionLocalProvider(TuIndiceDarkTheme.Local provides false) {
					currentInLight = AcademicStatusColors.current()
				}
			}
		}

		runOnIdle {
			assertEquals(SchemePrimary, currentInDark)
			assertEquals(SchemePrimary, currentInLight)
		}
	}

	@Composable
	private fun readStatusColors(): StatusColors {
		return StatusColors(
			approved = AcademicStatusColors.approved(),
			available = AcademicStatusColors.available(),
			blocked = AcademicStatusColors.blocked(),
			success = AcademicStatusColors.success(),
			warning = AcademicStatusColors.warning(),
			loadBandLight = AcademicStatusColors.loadBandLight(),
			loadBandManageable = AcademicStatusColors.loadBandManageable()
		)
	}

	private data class StatusColors(
		val approved: Color,
		val available: Color,
		val blocked: Color,
		val success: Color,
		val warning: Color,
		val loadBandLight: Color,
		val loadBandManageable: Color
	)

	private companion object {
		val SchemePrimary = Color(0xFF123456)

		val DarkStatusColors = StatusColors(
			approved = AcademicStatusColors.ApprovedDark,
			available = AcademicStatusColors.AvailableDark,
			blocked = AcademicStatusColors.BlockedDark,
			success = AcademicStatusColors.SuccessDark,
			warning = AcademicStatusColors.WarningDark,
			loadBandLight = AcademicStatusColors.LoadBandLightDark,
			loadBandManageable = AcademicStatusColors.LoadBandManageableDark
		)
		val LightStatusColors = StatusColors(
			approved = AcademicStatusColors.ApprovedLight,
			available = AcademicStatusColors.AvailableLight,
			blocked = AcademicStatusColors.BlockedLight,
			success = AcademicStatusColors.SuccessLight,
			warning = AcademicStatusColors.WarningLight,
			loadBandLight = AcademicStatusColors.LoadBandLightLight,
			loadBandManageable = AcademicStatusColors.LoadBandManageableLight
		)
	}
}
