package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignOutContentDialogUiTest {
	@Test
	fun when_confirmTapped_then_callsConfirmCallback() = runTuIndiceUiTest {
		var confirmClicks = 0

		setTuIndiceTestContent {
			SignOutContentDialog(
				state = SignOut.State.Plain,
				onConfirmClick = { confirmClicks++ },
				onSecondaryClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		assertEquals(1, confirmClicks)
	}

	@Test
	fun when_cancelTapped_then_callsDismissCallback() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			SignOutContentDialog(
				state = SignOut.State.Plain,
				onConfirmClick = {},
				onSecondaryClick = {},
				onDismissRequest = { dismissCalls++ }
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			dismissCalls > 0
		}

		assertTrue(dismissCalls >= 1)
	}
}
