package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ConfirmButtonUiTest {
	@Test
	fun when_notLoggingOut_then_isEnabledWithoutLoadingAndInvokesClick() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			ConfirmButton(
				text = "Cerrar sesion",
				isLoggingOut = false,
				onClick = { clicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeEnabled(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeHidden(
			tag = BaseUiTags.ConfirmationDialogPositiveLoading,
			useUnmergedTree = true
		)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton)
			.assertTextEquals("Cerrar sesion")
			.performClick()

		assertEquals(1, clicks)
	}

	@Test
	fun when_loggingOut_then_isDisabledAndShowsLoadingIndicator() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			ConfirmButton(
				text = "Cerrar sesion",
				isLoggingOut = true,
				onClick = { clicks++ }
			)
		}

		assertNodeDisabled(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeVisible(
			tag = BaseUiTags.ConfirmationDialogPositiveLoading,
			useUnmergedTree = true
		)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		assertEquals(0, clicks)
	}
}
