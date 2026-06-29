package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UsbIdTextFieldUiTest {
	@Test
	fun when_userTypesUsbId_then_formatsAndEmitsValue() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		assertNodeVisible(AuthUiTags.UsbIdTextField)
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("123")

		assertEquals("12-3", latestUsbId)
	}

	@Test
	fun when_userTypesNonNumericCharacters_then_emitsOnlyDigitsWithMask() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("ab12c3")

		assertEquals("12-3", latestUsbId)
	}

	@Test
	fun when_userTypesMoreThanAllowed_then_keepsMaxFormattedLength() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1234567")
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("89")

		assertEquals("12-34567", latestUsbId)
	}

	@Test
	fun when_usbIdChangesExternally_then_updatesDisplayedValue() = runTuIndiceUiTest {
		val usbId = mutableStateOf("12-34567")

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = usbId.value,
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> usbId.value = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).assertTextContains("12-34567")

		runOnIdle {
			usbId.value = "20-26123"
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).assertTextContains("20-26123")
	}

	@Test
	fun when_emailModeReceivesLetters_then_emitsTextWithoutMask() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "Correo USB",
				placeholderText = "correo@usb.ve",
				identifierMode = SignInIdentifierMode.UsbEmail,
				usbId = "",
				toggleContentDescription = "Usar USBID",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("mail@usb.ve")

		assertEquals("mail@usb.ve", latestUsbId)
	}

	@Test
	fun when_showTogglePulseIsTrueAndFieldIsEmpty_then_pulseIsVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				UsbIdTextField(
					labelText = "USB ID",
					placeholderText = "12-34567",
					usbId = "",
					toggleContentDescription = "Iniciar con correo USB",
					showTogglePulse = true,
					onIdentifierModeToggle = {},
					onUsbIdChange = {}
				)
			}
		}

		waitForIdle()

		assertNodeVisible(AuthUiTags.IdentifierModeTogglePulse, useUnmergedTree = true)
	}

	@Test
	fun when_showTogglePulseIsTrueButFieldHasValue_then_pulseIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "12-34567",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = true,
				onIdentifierModeToggle = {},
				onUsbIdChange = {}
			)
		}

		assertNodeHidden(AuthUiTags.IdentifierModeTogglePulse, useUnmergedTree = true)
	}
}
