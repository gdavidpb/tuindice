package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SignInLoggingInViewUiTest {
	@Test
	fun when_loggingInViewRendered_then_displaysContainerAndMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInLoggingInView(
				state = SignIn.State.LoggingIn(
					usbId = "12-34567",
					password = "1234",
					messages = listOf("Validando credenciales")
				)
			)
		}

		assertNodeVisible(AuthUiTags.SignInLoggingInContainer)
		assertNodeVisible(AuthUiTags.RandomFlipperText)
		onNodeWithText("Validando credenciales").assertIsDisplayed()
	}

	@Test
	fun when_loggingInHasMultipleMessages_then_displaysRandomFlipperContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInLoggingInView(
				state = SignIn.State.LoggingIn(
					usbId = "12-34567",
					password = "1234",
					messages = listOf(
						"Conectando con el servidor",
						"Validando credenciales"
					)
				)
			)
		}

		assertNodeVisible(AuthUiTags.SignInLoggingInContainer)
		assertNodeVisible(AuthUiTags.RandomFlipperText)
	}
}
