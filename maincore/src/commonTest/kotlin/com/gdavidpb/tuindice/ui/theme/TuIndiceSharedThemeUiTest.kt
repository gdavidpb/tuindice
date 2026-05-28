package com.gdavidpb.tuindice.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.TextUnit
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TuIndiceSharedThemeUiTest {
	@Test
	fun when_darkThemeDisabled_then_rendersContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSharedTheme(darkTheme = false) {
				Text("Tema claro")
			}
		}

		onNodeWithText("Tema claro").assertIsDisplayed()
	}

	@Test
	fun when_darkThemeEnabled_then_rendersContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSharedTheme(darkTheme = true) {
				Text("Tema oscuro")
			}
		}

		onNodeWithText("Tema oscuro").assertIsDisplayed()
	}

	@Test
	fun when_themeRendersMultipleChildren_then_displaysAllContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSharedTheme(darkTheme = false) {
				Text("Cabecera")
				Text("Contenido")
			}
		}

		onNodeWithText("Cabecera").assertIsDisplayed()
		onNodeWithText("Contenido").assertIsDisplayed()
	}

	@Test
	fun when_themeRenders_then_usesSharedDesignTokens() = runTuIndiceUiTest {
		var titleLargeFontSize = TextUnit.Unspecified
		var primaryColor = Color.Unspecified

		setTuIndiceTestContent {
			TuIndiceSharedTheme(darkTheme = false) {
				titleLargeFontSize = MaterialTheme.typography.titleLarge.fontSize
				primaryColor = MaterialTheme.colorScheme.primary
				Text("Tokens")
			}
		}

		waitForIdle()

		assertEquals(TuIndiceTypography.titleLarge.fontSize, titleLargeFontSize)
		assertEquals(TuIndiceColorScheme.light.primary, primaryColor)
	}
}
