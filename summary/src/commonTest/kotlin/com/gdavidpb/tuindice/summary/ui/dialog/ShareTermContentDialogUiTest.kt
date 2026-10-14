package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermContentDialogUiTest {
	@Test
	fun when_noTermHasClosed_then_theSheetSaysWhenItCanBeShared_andTheButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(terms = emptyList(), backgrounds = backgrounds()),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(onTermClick = {}, onBackgroundClick = {}, onShareClick = {}),
				onDismissRequest = {}
			)
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermEmpty).assertCountEquals(1)
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsNotEnabled()
	}

	@Test
	fun when_theRecordCannotBeRead_then_theSheetSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Unavailable(
					title = UiText.Raw("Compartir no disponible"),
					message = UiText.Raw("No pudimos leer tu registro en este momento.")
				),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(onTermClick = {}, onBackgroundClick = {}, onShareClick = {}),
				onDismissRequest = {}
			)
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermUnavailableTitle).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.ShareTermUnavailableMessage).assertCountEquals(1)
	}

	@Test
	fun when_theCardIsCaptured_then_sharingHandsItsPictureOver() = runTuIndiceUiTest {
		var shared: ImageBitmap? = null

		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(terms = terms(), backgrounds = backgrounds()),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(
					onTermClick = {},
					onBackgroundClick = {},
					onShareClick = { bitmap -> shared = bitmap }
				),
				onDismissRequest = {}
			)
		}

		// The button is enabled once the card of the chosen term has been captured over the chosen background.
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) {
			runCatching { onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled() }.isSuccess
		}
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).performClick()
		waitForIdle()

		assertEquals(CARD_WIDTH_PIXELS, checkNotNull(shared).width)
	}

	private fun terms() = listOf(
		ShareTermTermItem(
			key = "closed-2024-JAN_MAR",
			label = UiText.Raw("Ene - Mar 2024"),
			card = checkNotNull(
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.0)
					.toShareableTermCardItem(careerName = "Ingeniería de Computación")
			),
			selected = true
		)
	)

	private fun backgrounds() = listOf(
		ShareTermBackgroundItem(background = ShareBackground.included, selected = true),
		ShareTermBackgroundItem(background = otherShareBackground, selected = false)
	)

	private companion object {
		const val CARD_WIDTH_PIXELS = 1080
		const val CAPTURE_TIMEOUT_MILLIS = 5_000L
	}
}
