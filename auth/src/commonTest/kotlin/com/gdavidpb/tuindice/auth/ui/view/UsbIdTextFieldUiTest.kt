package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.mutableStateOf
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
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
				usbId = "",
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
				usbId = "",
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
				usbId = usbId.value,
				onUsbIdChange = { value -> usbId.value = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).assertTextContains("12-34567")

		runOnIdle {
			usbId.value = "20-26123"
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).assertTextContains("20-26123")
	}
}
