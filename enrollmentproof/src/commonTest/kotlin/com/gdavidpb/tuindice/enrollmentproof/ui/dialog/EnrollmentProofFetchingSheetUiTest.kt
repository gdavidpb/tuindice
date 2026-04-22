package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EnrollmentProofFetchingSheetUiTest {
	@Test
	fun when_fetchingSheetRendered_then_displaysLoadingAndMessage() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			EnrollmentProofFetchingSheet(
				cancelText = "Cancelar",
				messageText = "Descargando constancia de inscripcion...",
				onDismissRequest = { dismissCalls++ },
				loadingContent = {
					Text(text = "Cargando")
				}
			)
		}

		assertNodeVisible(EnrollmentProofUiTags.FetchingSheet)
		assertNodeVisible(EnrollmentProofUiTags.FetchingLoadingContainer)
		assertNodeVisible(EnrollmentProofUiTags.FetchingMessage)
		assertNodeVisible(EnrollmentProofUiTags.FetchingCancelButton)
		onNodeWithTag(EnrollmentProofUiTags.FetchingSheet).assertIsDisplayed()
		onNodeWithText("Cargando").assertIsDisplayed()
		onNodeWithText("Descargando constancia de inscripcion...").assertIsDisplayed()
		onNodeWithText("Cancelar").assertIsDisplayed()
		assertEquals(0, dismissCalls)
	}

	@Test
	fun when_customLoadingComposableProvided_then_rendersCustomLoadingNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentProofFetchingSheet(
				cancelText = "Cancelar",
				messageText = "Descargando...",
				onDismissRequest = {},
				loadingContent = {
					Box(
						modifier = Modifier
							.size(1.dp)
							.testTag("enrollment_custom_loading")
					)
				}
			)
		}

		assertNodeVisible("enrollment_custom_loading")
		assertNodeVisible(EnrollmentProofUiTags.FetchingMessage)
	}

	@Test
	fun when_cancelButtonClicked_then_invokesDismissRequest() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			EnrollmentProofFetchingSheet(
				cancelText = "Cancelar",
				messageText = "Descargando...",
				onDismissRequest = { dismissCalls++ },
				loadingContent = {
					Text(text = "Cargando")
				}
			)
		}

		onNodeWithTag(EnrollmentProofUiTags.FetchingCancelButton).performClick()

		assertEquals(1, dismissCalls)
	}
}
