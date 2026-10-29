package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SettingsRowUiTest {
	@Test
	fun when_actionRowIsTapped_then_itRunsItsActionOnce() = runTuIndiceUiTest {
		var taps = 0
		setTuIndiceTestContent {
			SettingsActionRow(
				icon = ColorPainter(Color.Gray),
				text = "Borrar todo lo que recuerda",
				testTag = TAG,
				isDestructive = true,
				onClick = { taps++ }
			)
		}

		assertNodeVisible(TAG)
		onNodeWithTag(TAG).performClick()

		assertEquals(1, taps)
	}

	@Test
	fun when_rowHasADetailLine_then_bothLinesAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsActionRow(
				icon = ColorPainter(Color.Gray),
				text = "Compartir\nInvita a un amigo a descargar la aplicación",
				testTag = TAG,
				isDestructive = false,
				onClick = {}
			)
		}

		onNodeWithText("Compartir", substring = true).assertIsDisplayed()
		onNodeWithText("Invita a un amigo", substring = true).assertIsDisplayed()
	}

	@Test
	fun when_linkRowIsTapped_then_itRunsItsActionOnce() = runTuIndiceUiTest {
		var opens = 0
		setTuIndiceTestContent {
			SettingsLinkRow(
				icon = ColorPainter(Color.Gray),
				text = "Política de privacidad\nConoce cómo manejamos tus datos",
				testTag = TAG,
				onClick = { opens++ }
			)
		}

		onNodeWithTag(TAG).performClick()

		assertEquals(1, opens)
	}

	private companion object {
		const val TAG = "base_test_settings_row"
	}
}
