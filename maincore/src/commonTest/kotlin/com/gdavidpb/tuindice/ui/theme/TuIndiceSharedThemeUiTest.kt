package com.gdavidpb.tuindice.ui.theme

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

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
}
