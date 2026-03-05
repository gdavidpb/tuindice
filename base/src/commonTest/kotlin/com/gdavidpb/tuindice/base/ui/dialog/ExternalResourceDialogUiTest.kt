package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ExternalResourceDialogUiTest {
	@Test
	fun when_confirmTapped_then_emitsOriginalUrl() = runTuIndiceUiTest {
		val expectedUrl = "https://tuindice.app/privacy"
		var confirmedUrl = ""

		setTuIndiceTestContent {
			ExternalResourceDialog(
				url = expectedUrl,
				titleText = "Abrir enlace",
				messageText = "Se abrira un recurso externo",
				openText = "Abrir",
				cancelText = "Cancelar",
				onConfirmClick = { url -> confirmedUrl = url },
				onDismissRequest = {}
			)
		}

		assertNodeVisible(BaseUiTags.ExternalResourceMessage)
		assertNodeVisible(BaseUiTags.ExternalResourceUrl)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitForIdle()

		assertEquals(expectedUrl, confirmedUrl)
	}

	@Test
	fun when_cancelTapped_then_invokesDismissRequest() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			ExternalResourceDialog(
				url = "https://tuindice.app/privacy",
				titleText = "Abrir enlace",
				messageText = "Se abrira un recurso externo",
				openText = "Abrir",
				cancelText = "Cancelar",
				onConfirmClick = {},
				onDismissRequest = { dismissCalls++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			dismissCalls > 0
		}

		assertTrue(dismissCalls >= 1)
	}
}
