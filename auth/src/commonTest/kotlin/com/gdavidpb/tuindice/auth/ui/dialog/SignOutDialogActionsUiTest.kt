package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
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
class SignOutDialogActionsUiTest {
	@Test
	fun when_secondaryTextIsNull_then_showsOnlyCancelAndConfirmActions() = runTuIndiceUiTest {
		val calls = mutableListOf<String>()

		setTuIndiceTestContent {
			SignOutDialogActions(
				confirmText = "Cerrar sesion",
				secondaryText = null,
				cancelText = "Cancelar",
				isLoggingOut = false,
				onConfirmClick = { calls += "confirm" },
				onSecondaryClick = { calls += "secondary" },
				onDismissRequest = { calls += "dismiss" }
			)
		}

		assertNodeHidden(AuthUiTags.SignOutSecondaryButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton)
			.assertTextEquals("Cancelar")
			.performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton)
			.assertTextEquals("Cerrar sesion")
			.performClick()

		assertEquals(listOf("dismiss", "confirm"), calls)
	}

	@Test
	fun when_secondaryTextProvided_then_routesEachActionToItsCallback() = runTuIndiceUiTest {
		val calls = mutableListOf<String>()

		setTuIndiceTestContent {
			SignOutDialogActions(
				confirmText = "Cerrar sesion",
				secondaryText = "Cambiar clave",
				cancelText = "Cancelar",
				isLoggingOut = false,
				onConfirmClick = { calls += "confirm" },
				onSecondaryClick = { calls += "secondary" },
				onDismissRequest = { calls += "dismiss" }
			)
		}

		assertNodeVisible(AuthUiTags.SignOutSecondaryButton)
		onNodeWithTag(AuthUiTags.SignOutSecondaryButton)
			.assertTextEquals("Cambiar clave")
			.performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		assertEquals(listOf("secondary", "dismiss", "confirm"), calls)
	}

	@Test
	fun when_loggingOut_then_disablesEveryActionAndShowsLoading() = runTuIndiceUiTest {
		val calls = mutableListOf<String>()

		setTuIndiceTestContent {
			SignOutDialogActions(
				confirmText = "Cerrar sesion",
				secondaryText = "Cambiar clave",
				cancelText = "Cancelar",
				isLoggingOut = true,
				onConfirmClick = { calls += "confirm" },
				onSecondaryClick = { calls += "secondary" },
				onDismissRequest = { calls += "dismiss" }
			)
		}

		assertNodeDisabled(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeDisabled(BaseUiTags.ConfirmationDialogNegativeButton)
		assertNodeDisabled(AuthUiTags.SignOutSecondaryButton)
		assertNodeVisible(
			tag = BaseUiTags.ConfirmationDialogPositiveLoading,
			useUnmergedTree = true
		)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		onNodeWithTag(AuthUiTags.SignOutSecondaryButton).performClick()

		assertEquals(emptyList(), calls)
	}

	@Test
	fun when_notLoggingOut_then_everyActionIsEnabledWithoutLoading() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignOutDialogActions(
				confirmText = "Cerrar sesion",
				secondaryText = "Cambiar clave",
				cancelText = "Cancelar",
				isLoggingOut = false,
				onConfirmClick = {},
				onSecondaryClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeEnabled(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeEnabled(BaseUiTags.ConfirmationDialogNegativeButton)
		assertNodeEnabled(AuthUiTags.SignOutSecondaryButton)
		assertNodeHidden(
			tag = BaseUiTags.ConfirmationDialogPositiveLoading,
			useUnmergedTree = true
		)
	}
}
