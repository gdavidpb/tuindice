package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CancelButtonUiTest {
	@Test
	fun when_enabledButtonTapped_then_invokesClickCallback() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			CancelButton(
				text = "Cancelar",
				enabled = true,
				onClick = { clicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogNegativeButton)
		assertNodeEnabled(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton)
			.assertTextEquals("Cancelar")
			.performClick()

		assertEquals(1, clicks)
	}

	@Test
	fun when_buttonDisabled_then_ignoresTap() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			CancelButton(
				text = "Cancelar",
				enabled = false,
				onClick = { clicks++ }
			)
		}

		assertNodeDisabled(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton)
			.assertTextEquals("Cancelar")
			.performClick()

		assertEquals(0, clicks)
	}
}
