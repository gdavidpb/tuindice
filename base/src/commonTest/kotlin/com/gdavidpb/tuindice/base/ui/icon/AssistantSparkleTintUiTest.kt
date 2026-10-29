package com.gdavidpb.tuindice.base.ui.icon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AssistantSparkleTintUiTest {
	@Test
	fun when_theSparkleIsTinted_then_itIsThePrimaryOfTheSchemeInLightAndDark() = runTuIndiceUiTest {
		var lightTint: Color? = null
		var darkTint: Color? = null

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides false) {
				MaterialTheme(colorScheme = lightColorScheme(primary = LightPrimary)) {
					lightTint = assistantSparkleTint()
				}
			}
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides true) {
				MaterialTheme(colorScheme = darkColorScheme(primary = DarkPrimary)) {
					darkTint = assistantSparkleTint()
				}
			}
		}

		runOnIdle {
			assertEquals(LightPrimary, lightTint)
			assertEquals(DarkPrimary, darkTint)
		}
	}

	@Test
	fun when_theThemeSwitches_then_theTintFollowsThePrimary() = runTuIndiceUiTest {
		val colorScheme = mutableStateOf(lightColorScheme(primary = LightPrimary))
		var tint: Color? = null

		setTuIndiceTestContent {
			MaterialTheme(colorScheme = colorScheme.value) {
				tint = assistantSparkleTint()
			}
		}

		runOnIdle {
			assertEquals(LightPrimary, tint)
			colorScheme.value = darkColorScheme(primary = DarkPrimary)
		}

		runOnIdle {
			assertEquals(DarkPrimary, tint)
		}
	}

	private companion object {
		val LightPrimary = Color(0xFFFABD00)
		val DarkPrimary = Color(0xFFFFD54F)
	}
}
