package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TuIndiceDarkThemeUiTest {
	@Test
	fun when_localForcesATheme_then_isDarkReturnsTheForcedValue() = runTuIndiceUiTest {
		var forcedDark: Boolean? = null
		var forcedLight: Boolean? = null

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides true) {
				forcedDark = TuIndiceDarkTheme.isDark()
			}
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides false) {
				forcedLight = TuIndiceDarkTheme.isDark()
			}
		}

		runOnIdle {
			assertEquals(true, forcedDark)
			assertEquals(false, forcedLight)
		}
	}

	@Test
	fun when_localIsUnset_then_isDarkFollowsTheSystemTheme() = runTuIndiceUiTest {
		var systemDark: Boolean? = null
		var withoutProvider: Boolean? = null
		var resetInsideForcedTheme: Boolean? = null

		setTuIndiceTestContent {
			val isSystemDark = isSystemInDarkTheme()

			systemDark = isSystemDark
			withoutProvider = TuIndiceDarkTheme.isDark()

			CompositionLocalProvider(TuIndiceDarkTheme.Local provides !isSystemDark) {
				CompositionLocalProvider(TuIndiceDarkTheme.Local provides null) {
					resetInsideForcedTheme = TuIndiceDarkTheme.isDark()
				}
			}
		}

		runOnIdle {
			assertEquals(systemDark, withoutProvider)
			// A null override hands the decision back to the system, even under a forced ancestor.
			assertEquals(systemDark, resetInsideForcedTheme)
		}
	}

	@Test
	fun when_forcedValueChanges_then_isDarkRecomposesWithTheNewValue() = runTuIndiceUiTest {
		val forced = mutableStateOf<Boolean?>(true)
		val observed = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides forced.value) {
				val isDark = TuIndiceDarkTheme.isDark()

				if (observed.lastOrNull() != isDark) observed += isDark
			}
		}

		runOnIdle {
			assertEquals(listOf(true), observed)
			forced.value = false
		}

		runOnIdle {
			assertEquals(listOf(true, false), observed)
		}
	}
}
