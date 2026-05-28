package com.gdavidpb.tuindice.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class GooglePlayServicesDialogUiTest {
	@Test
	fun when_confirmExitTapped_then_invokesConfirmCallback() = runTuIndiceUiTest {
		var confirmClicks = 0

		setTuIndiceTestContent {
			GooglePlayServicesDialog(
				titleText = "Servicios de Google Play",
				messageText = "No estan disponibles en este dispositivo.",
				exitText = "Salir",
				onConfirmExitClick = { confirmClicks++ },
				onDismissRequest = {}
			)
		}

		assertNodeVisible(MaincoreUiTags.GooglePlayServicesMessage)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			confirmClicks == 1
		}

		assertEquals(1, confirmClicks)
	}

	@Test
	fun when_confirmExitTapped_then_invokesDismissCallbackAndHidesNegativeButton() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			GooglePlayServicesDialog(
				titleText = "Servicios de Google Play",
				messageText = "No estan disponibles en este dispositivo.",
				exitText = "Salir",
				onConfirmExitClick = {},
				onDismissRequest = { dismissCalls++ }
			)
		}

		assertNodeVisible(MaincoreUiTags.GooglePlayServicesMessage)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeHidden(BaseUiTags.ConfirmationDialogNegativeButton)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			dismissCalls > 0
		}

		assertEquals(1, dismissCalls)
	}

	@Test
	fun when_dialogRendered_then_displaysConfiguredTitle() = runTuIndiceUiTest {
		val title = "Servicios de Google Play"

		setTuIndiceTestContent {
			GooglePlayServicesDialog(
				titleText = title,
				messageText = "No estan disponibles en este dispositivo.",
				exitText = "Salir",
				onConfirmExitClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogTitle)
		onNodeWithText(title).assertIsDisplayed()
	}
}
