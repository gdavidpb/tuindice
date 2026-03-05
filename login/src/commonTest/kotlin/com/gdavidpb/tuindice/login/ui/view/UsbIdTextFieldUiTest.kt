package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.login.ui.LoginUiTags
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
				usbId = "",
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		assertNodeVisible(LoginUiTags.UsbIdTextField)
		onNodeWithTag(LoginUiTags.UsbIdTextField).performTextInput("123")

		assertEquals("12-3", latestUsbId)
	}

	@Test
	fun when_userTypesNonNumericCharacters_then_emitsOnlyDigitsWithMask() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				usbId = "",
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(LoginUiTags.UsbIdTextField).performTextInput("ab12c3")

		assertEquals("12-3", latestUsbId)
	}

	@Test
	fun when_userTypesMoreThanAllowed_then_keepsMaxFormattedLength() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				usbId = "",
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(LoginUiTags.UsbIdTextField).performTextInput("1234567")
		onNodeWithTag(LoginUiTags.UsbIdTextField).performTextInput("89")

		assertEquals("12-34567", latestUsbId)
	}
}
