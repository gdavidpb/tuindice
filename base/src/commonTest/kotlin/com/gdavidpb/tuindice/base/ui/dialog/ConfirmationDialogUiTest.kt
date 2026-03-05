package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class ConfirmationDialogUiTest {
	@Test
	fun when_positiveButtonTappedWithoutDismiss_then_invokesPositiveCallback() = runTuIndiceUiTest {
		var positiveClicks = 0

		setTuIndiceTestContent {
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			LaunchedEffect(Unit) {
				sheetState.show()
			}

			ConfirmationDialog(
				sheetState = sheetState,
				titleText = "Eliminar evaluacion",
				positiveText = "Aceptar",
				negativeText = "Cancelar",
				dismissOnPositive = false,
				onPositiveClick = { positiveClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogSheet)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		assertEquals(1, positiveClicks)
	}

	@Test
	fun when_positiveIsLoading_then_showsLoaderAndDisablesPositiveButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			LaunchedEffect(Unit) {
				sheetState.show()
			}

			ConfirmationDialog(
				sheetState = sheetState,
				titleText = "Cargando",
				positiveText = "Aceptar",
				positiveLoading = true,
				positiveEnabled = false
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveLoading)
		assertNodeDisabled(BaseUiTags.ConfirmationDialogPositiveButton)
	}

	@Test
	fun when_negativeButtonTapped_then_invokesNegativeAndDismissCallbacks() = runTuIndiceUiTest {
		var negativeClicks = 0
		var dismissCalls = 0

		setTuIndiceTestContent {
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			LaunchedEffect(Unit) {
				sheetState.show()
			}

			ConfirmationDialog(
				sheetState = sheetState,
				titleText = "Eliminar evaluacion",
				positiveText = "Aceptar",
				negativeText = "Cancelar",
				onNegativeClick = { negativeClicks++ },
				onDismissRequest = { dismissCalls++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			negativeClicks > 0 && dismissCalls > 0
		}

		assertEquals(1, negativeClicks)
		assertEquals(1, dismissCalls)
	}

	@Test
	fun when_customContentProvided_then_displaysInsideSheet() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			LaunchedEffect(Unit) {
				sheetState.show()
			}

			ConfirmationDialog(
				sheetState = sheetState,
				titleText = "Titulo",
				positiveText = "Aceptar",
				negativeText = "Cancelar"
			) {
				androidx.compose.material3.Text("Contenido personalizado")
			}
		}

		onNodeWithText("Contenido personalizado").assertIsDisplayed()
	}
}
